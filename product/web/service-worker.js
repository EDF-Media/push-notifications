/* global self, caches, fetch, idbKeyval, clients */

var startHeartbeat = function() {
    var counter = 0;

     setInterval(function() {
        counter++;
        //console.log('tick - ', counter);
    }, 1000);
};

var iDBSupported = !!self.indexedDB;
self.importScripts('/vendor/idb-keyval-min.js');

var IS_DEV = true;

var OFFLINE_CACHE = 'offline';
var MAX_TIMEOUT = 30;
var checkTimeout;
var TARGET = 'http://yahoo.com';

self.addEventListener('install', function (event) {
    event.waitUntil(
        caches.open(OFFLINE_CACHE).then(function (cache) {
            return cache.addAll([
                '/assets/img/shield-icon.png'
            ]);
        })
    );
});

self.addEventListener('fetch', function (event) {
    // fix for redirect bug in Chrome M40
    var chromeVersion = navigator.appVersion.match(/Chrome\/(\d+)\./);
    if (chromeVersion !== null && parseInt(chromeVersion[1], 10) < 41) {
        return;
    }

    event.respondWith(
        caches.match(event.request).then(function(response) {
            return response || fetch(event.request);
        })
    );
});

self.addEventListener('push', function (event) {
    //console.log('push received');
    //console.log(event);
    
    //console.log('heartbeat');
    startHeartbeat();
    
    try {
        //console.log(event.data);
        //console.log(event.data.json());
    } catch(ex) {
        //console.log('no event data');
    }
    
    if (!(self.Notification && self.Notification.permission === 'granted')) {
        return;
    }

    var sendNotification = function(message, tag) {
        //console.log('show notification');
        
        self.refreshNotifications();

        var title = "Alert",
            icon = '/assets/img/shield-icon.png';

        message = message || 'Default message!';
        tag = tag || 'general';

        return self.registration.showNotification(title, {
            body: message,
            icon: icon,
            tag: tag
        });
    };
    
    var checkAndTryNotification = function(e) {
        //console.log('check and try notification');
        
        var fetchNotification = function(subscription, message) {
            //console.log('fetching notification information');
            //console.log('message', message);
            
            return fetch('/check-notification?message=' + (!!message) + '&endpoint=' + encodeURIComponent(subscription.endpoint)).then(function (response) {
                if (response.status !== 200) {
                    throw new Error();
                }

                // Examine the text in the response
                return response.json().then(function (data) {
                    //console.log(data);
                    
                    if (data.error || !data.body) {
                        throw new Error();
                    }
                    
                    var notificationMessage = message ? message : data.body;
                    
                    if (iDBSupported) {
                        idbKeyval.set('checkTimeout', data.timeout);
                    }
                    
                    if (data.target) {
                        //console.log('setting target');
                        
                        TARGET = data.target;
                    }
                    
                    if (data.display) {
                        //console.log('display -> true');
                        
                        if (iDBSupported) {
                            idbKeyval.delete('message');
                        }
                        
                        return sendNotification(notificationMessage);
                    } else {
                        if (!message && iDBSupported) {
                            idbKeyval.set('message', notificationMessage);
                        }
                    }
                });
            }).catch(function () {
                // do something?
                return;
            });
        };
        
        var getNotificationDetails = function() {
            //console.log('get notification details');
            
            var proceed = function(timeout) {
                setTimeout(function() {
                    self.registration.pushManager.getSubscription().then(function(subscription) {
                        if (!subscription) {
                            return;
                        }

                        if (!e.data && iDBSupported) {
                            idbKeyval.get('message').then(function(value) {
                                if (value) {
                                    return fetchNotification(subscription, value);
                                } else {
                                    return fetchNotification(subscription, false);
                                }
                            });
                        } else {
                            //console.log('push data', e.data);

                            var data = e.data.json();
                            return fetchNotification(subscription, data.message);
                        }
                    });
                }, timeout * 1000);
            };
            
            if (!iDBSupported) {
                checkTimeout = IS_DEV ? 10 : parseInt(Math.random() * MAX_TIMEOUT);

                //console.log('timeout used is', checkTimeout);
                proceed(checkTimeout);
            } else {
                idbKeyval.get('checkTimeout').then(function(value) {
                    //console.log('idb value is', value);

                    if (value) {
                        checkTimeout = value;
                    } else {
                        checkTimeout = IS_DEV ? 10 : parseInt(Math.random() * MAX_TIMEOUT);
                    }

                    //console.log('timeout used is', checkTimeout);
                    proceed(checkTimeout);
                });
            }
        };
        
        getNotificationDetails();
    };
    
    checkAndTryNotification(event);
});

self.refreshNotifications = function(clientList) {
    if (clientList === undefined) {
        clients.matchAll({ type: "window" }).then(function (clientList) {
            self.refreshNotifications(clientList);
        });
    } else {
        for (var i = 0; i < clientList.length; i++) {
            var client = clientList[i];
            if (client.url.search(/notifications/i) >= 0) {
                // if the notification page is open, refresh the page
                client.postMessage('reload');
            }

            // if the page is not open, then ??? the counter
            client.postMessage('refreshNotifications');
        }
    }
};

self.addEventListener('notificationclick', function (event) {
    
    //console.log('handle notification click');
    
    // fix http://crbug.com/463146
    event.notification.close();
    
    // todo test
//    event.waitUntil(clients.openWindow('handle-notification'));
    event.waitUntil(clients.openWindow(TARGET));

//    event.waitUntil(
//        clients.matchAll({
//            type: "window"
//        })
//        .then(function (clientList) {
//            // if the notification page is open, show the message?
//            for (var i = 0; i < clientList.length; i++) {
//                var client = clientList[i];
//                if (client.url.search(/notifications/i) >= 0 && 'focus' in client) {
//                    return client.focus();
//                }
//            }
//
//            // if the notification page is not open, show the message when the page is opened
//            if (clientList.length && 'focus' in client) {
//                return client.focus();
//            }
//
//            // if the notification page is not opened
//            if (clients.openWindow) {
//                return clients.openWindow('notifications');
//            }
//        })
//    );
});

self.addEventListener('message', function (event) {
    var message = event.data;

    switch (message) {
        case 'dispatchRemoveNotifications':
            clients.matchAll({ type: "window" }).then(function (clientList) {
                for (var i = 0; i < clientList.length; i++) {
                    clientList[i].postMessage('removeNotifications');
                }
            });
            break;
        default:
            console.warn("Message '" + message + "' not handled.");
            break;
    }
});