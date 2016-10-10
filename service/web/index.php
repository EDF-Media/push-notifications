<?php

require_once __DIR__.'/../vendor/autoload.php';

$app = new Silex\Application();
$app->register(new MongoDBServiceProvider(), [
    'mongodb.config' => [
        'server' => 'mongodb://localhost:27017',
        'options' => [],
        'driverOptions' => [],
    ]
]);

$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/../views',
));

$app->get('/', function () use ($app) {
    if(!isset($_GET['action']) || !in_array($_GET['action'], array('delete', 'update', 'create'))) {
        die('ok');
    }
    
    $data = json_decode(file_get_contents('php://input'));
    if(!iseet($data['endpoint']) || !iseet($data['key']) || !iseet($data['token'])) {
        die('okz');
    }
     
    if($_GET['action'] == 'create') {
        $app['mongodb']
          ->push
          ->users
          ->insert(array(
              '_id' => md5($data['endpoint']),
              'timestamp' => time(),
              'endpoint' => $data['endpoint'],
              'key' => $data['key'],
              'token' => $data['token']
          ));        
    }
    
    if($_GET['action'] == 'update') {
        $app['mongodb']
            ->push
            ->users
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
        $app['mongodb']
            ->push
            ->users
            ->deleteOne(array(
                '_id' => md5($data['endpoint'])
            ));
    }    
    
    die('ok');
});

$app->run();
