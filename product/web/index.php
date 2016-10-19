<?php

require_once __DIR__.'/../vendor/autoload.php';

use Symfony\Component\HttpFoundation\Response;

$parts = explode('.', $_SERVER['SERVER_NAME']);
end($parts);

$currentDomain = prev($parts);

$themeMapping = array(
    'virus-notification' => 'theme-1', 
    'droidspeed' => 'theme-2'
);

$app = new Silex\Application();

$app['debug'] = true;
$activeTheme = $themeMapping[$currentDomain];

$app->register(new Silex\Provider\TwigServiceProvider(), array(
    'twig.path' => __DIR__.'/../views',
));

$app->get('/home', function () use ($app, $activeTheme) {
    return $app['twig']->render("lp/{$activeTheme}-lp.html.twig");
});

$app->get('/allow', function () use ($app, $activeTheme) {
    return $app['twig']->render("allow/{$activeTheme}-allow.html.twig");
});

$app->get('/check-notification', function () {
    return new Response(json_encode(array(
        'body' => 'Server notification message!',
        'target' => 'https://google.com',
        'display' => true,
        'timeout' => 10
    )), 200, array('Content-Type' => 'application/json'));
});

$app->get('/register-subscription', function () {
    return new Response('Ok', 200);
});

$app->post('/register-subscription', function () {
    return new Response('Ok', 200);
});

$app->run();
