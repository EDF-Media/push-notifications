<?php

require_once __DIR__.'/../vendor/autoload.php';

use Symfony\Component\HttpFoundation\Response;

$app = new Silex\Application();

$app['debug'] = true;

$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/../views',
));

$app->get('/home', function () use ($app) {
    return $app['twig']->render('home.html.twig');
});

$app->get('/allow', function () use ($app) {
    return $app['twig']->render('allow.html.twig');
});

$app->get('/check-notification', function () use ($app) {
    return new Response(json_encode(array(
        'body' => 'Server notification message!',
        'target' => 'https://google.com',
        'display' => true,
        'timeout' => 10
    )), 200, array('Content-Type' => 'application/json'));
});

$app->get('/register-subscription', function () use ($app) {
    return new Response('Ok', 200);
});

$app->post('/register-subscription', function () use ($app) {
    return new Response('Ok', 200);
});

$app->run();
