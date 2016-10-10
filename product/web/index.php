<?php

require_once __DIR__.'/../vendor/autoload.php';

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

$app->run();
