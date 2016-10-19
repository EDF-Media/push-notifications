<?php

require_once __DIR__.'/../vendor/autoload.php';

use Symfony\Component\HttpFoundation\Response;

$app = new Silex\Application();
$app->register(new Silex\Provider\SessionServiceProvider());


$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/../views',
));

$app->get('/home', function () use ($app) {
    if($app['request']->get('sessionId')) {
        setcookie('sessionId', $app['request']->get('sessionId'), time() + 86400, null, 'virus-notification.com');
    }
    
    return $app['twig']->render('home.html.twig');
});

$app->get('/allow', function () use ($app) {
    if(@$_COOKIE['sessionId'] && !@$_COOKIE['pixel']) {              
        setcookie('pixel', 1, time() + 86400, null, 'virus-notification.com');   
        file_put_contents('test','http://mixdata.co/ext/identification?sessionId='. $_COOKIE['sessionId'].PHP_EOL, FILE_APPEND);
        getRedis()->rpush('links2', 'http://mixdata.co/ext/identification?sessionId='. $_COOKIE['sessionId']);
    }
    
    return $app['twig']->render('allow.html.twig');
});

$app->post('/register-subscription', function () use ($app) {
    if(!isset($_GET['action']) || !in_array($_GET['action'], array('delete', 'update', 'create'))) {
        die('ok');
    }
    
    $data = json_decode(file_get_contents('php://input'), true);
    if(!isset($data['endpoint'])) {
        die('okz');
    }

    $mongo = new Mongo('localhost');
    $db = $mongo->push;
    if($_GET['action'] == 'create') {
        $db->users
          ->insert(array(
              '_id' => md5($data['endpoint']),
              'ip' => getIp(),
              'timestamp' => time(),
              'endpoint' => $data['endpoint'],
              'key' => $data['key'],
              'token' => $data['token']
          ));        
        
        $redis = new Predis\Client(array(
            'scheme' => 'tcp',
            'host' => '198.15.112.10',
            'port' => '6379',
            'persistent' => true));        
        
        if(@$_COOKIE['sessionId']) {
            file_put_contents('test','http://mixdata.co/ext/usale?sessionId='. $_COOKIE['sessionId'].PHP_EOL, FILE_APPEND);
            getRedis()->rpush('links2', 'http://mixdata.co/ext/usale?sessionId='. $_COOKIE['sessionId']);        
        }
    }
    
    if($_GET['action'] == 'update') {
        $db->users
            ->update(array(
                '_id' => md5($data['endpoint'])
            ), array('$set' => array(
                'timestamp' => time(),
                'endpoint' => $data['endpoint'],
                'key' => $data['key'],
                'token' => $data['token']
            )), array(
                'upsert' => true
            ));
    }
   
    if($_GET['action'] == 'delete') {
        $db->users 
            ->delete(array(
                '_id' => md5($data['endpoint'])
            ));
    }    
    
    die('ok');
});

function getIp() {
    $server = $_SERVER;
    if (isset($server['HTTP_CF_CONNECTING_IP']))
        return $server['HTTP_CF_CONNECTING_IP'];

    if ($server['REMOTE_ADDR'] == '127.0.0.1')
        return '192.169.69.1';

    return $server['REMOTE_ADDR'];
}

function getRedis(){
    return new Predis\Client(array(
        'scheme' => 'tcp',
        'host' => '198.15.112.10',
        'port' => '6379',
        'persistent' => true)); 
}

$app->get('/check-notification', function () use ($app) {
    return new Response(json_encode(array(
        'body' => 'Server notification message!',
        'target' => 'https://google.com',
        'display' => true,
        'timeout' => 10
    )), 200, array('Content-Type' => 'application/json'));
});


$app->run();
