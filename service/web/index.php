<?php

require_once __DIR__.'/../vendor/autoload.php';

$app = new Silex\Application();

$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/../views',
));

$app->post('/api/client', function () use ($app) {
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
              'timestamp' => time(),
              'endpoint' => $data['endpoint'],
              'key' => $data['key'],
              'token' => $data['token']
          ));        
    }
    
    if($_GET['action'] == 'update') {
        $db->users
            ->updateOne(array(
                '_id' => md5($data['endpoint'])
            ), array(
                'timestamp' => time(),
                'endpoint' => $data['endpoint'],
                'key' => $data['key'],
                'token' => $data['token']
            ), array(
                'upsert' => true
            ));
    }
   
    if($_GET['action'] == 'delete') {
        $db->users 
            ->deleteOne(array(
                '_id' => md5($data['endpoint'])
            ));
    }    
    
    die('ok');
});

$app->run();
