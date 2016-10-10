var isPushEnabled = true;

window.addEventListener('load', function() {
    push_subscribe();

    // double notification badge only on mobile layout
    var navbarToggle = $('.navbar-toggle:visible');
    if(navbarToggle) {
        var notificationCounters = document.getElementsByClassName('notificationCounter');

        if (notificationCounters.length) {
            var notificationCounterWrapperNavbar = document.querySelector('.navbar-toggle .notificationCounterWrapper');

            var notificationCounter = document.createElement('span');
            notificationCounter.className = 'notificationCounter badge badge-notification';
            notificationCounter.textContent = notificationCounters[0].textContent;
            notificationCounterWrapperNavbar.appendChild(notificationCounter);
        }
    }

    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/service-worker.js')
        .then(function(sw) {
            console.log('[SW] Service worker registered.');
            push_initialiseState();
            initPostMessageListener();
        }, function (e) {
            console.error('[SW] Oops...', e);

        });
    } else {
        console.warn('[SW] Service workers are not supported by your browser.');
    }
});

function initPostMessageListener() {
    var onRefreshNotifications = function () {
        var notificationCounters = document.getElementsByClassName('notificationCounter');

        if (!notificationCounters.length) {
            var notificationCounterWrappers = document.getElementsByClassName('notificationCounterWrapper');

            for (var i = 0; i < notificationCounterWrappers.length; i++) {
                var notificationCounter = document.createElement('span');
                notificationCounter.className = 'notificationCounter badge badge-notification';
                notificationCounter.textContent = '0';
                notificationCounterWrappers[i].appendChild(notificationCounter);
            }
        }

        for (var i = 0; i < notificationCounters.length; i++) {
            notificationCounters[i].textContent++;
        }
    };

    var onRemoveNotifications = function() {
        $('.notificationCounter').remove();
    };

    navigator.serviceWorker.addEventListener('message', function(e) {
        var message = e.data;

        switch (message) {
            case 'reload':
                window.location.reload(true);
                break;
            case 'refreshNotifications':
                onRefreshNotifications();
                break;
            case 'removeNotifications':
                onRemoveNotifications();
                break;
            default:
                console.warn("Message '" + message + "' not handled.");
                break;
        }
    });
}

function push_initialiseState() {
    // Are Notifications supported in the service worker?
    if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
        console.warn('[SW] Push notifications are not supported in this browser.');
        return;
    }

    // Check the current Notification permission.
    // If its denied, it's a permanent block until the
    // user changes the permission
    if (Notification.permission === 'denied') {
        console.warn('[SW] Denied.');
        retryNotification();
        return;
    }

    // Check if push messaging is supported
    if (!('PushManager' in window)) {
        console.warn('[SW] Push notifications are not supported in this browser.');
        return;
    }

    // We need the service worker registration to check for a subscription
    navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
        // Do we already have a push message subscription?
        serviceWorkerRegistration.pushManager.getSubscription()
        .then(function(subscription) {
            if (!subscription) {
                // We aren't subscribed to push, so set UI
                // to allow the user to enable push
                return;
            }

            // Keep your server in sync with the latest endpoint
            push_sendSubscriptionToServer(subscription, 'update');

        })
        ['catch'](function(err) {
            console.warn('[SW] Error occured during getSubscription()', err);
        });
    });
}

function push_subscribe() {
    navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.subscribe({userVisibleOnly: true})
        .then(function(subscription) {
            // The subscription was successful
            return push_sendSubscriptionToServer(subscription, 'create');
            pixel_call('http://mixdata.co/pixel/notifications/allow');
        })
        ['catch'](function(e) {
            if (Notification.permission === 'denied') {
                // The user denied the notification permission which
                // means we failed to subscribe and the user will need
                // to manually change the notification permission to
                // subscribe to push messages
                retryNotification();
                console.warn('[SW] Denied.');
            } else {
                console.warn('[SW] Funny, retry it.', e);
                push_subscribe();
                shake();
            }
        });
    });
}

function push_unsubscribe() {
  navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
    // To unsubscribe from push messaging, you need get the
    // subscription object, which you can call unsubscribe() on.
    serviceWorkerRegistration.pushManager.getSubscription().then(
      function(pushSubscription) {
        // Check we have a subscription to unsubscribe
        if (!pushSubscription) {
            // No subscription object, so set the state
            // to allow the user to subscribe to push
          return;
        }

        push_sendSubscriptionToServer(pushSubscription, 'delete');

        // We have a subscription, so call unsubscribe on it
        pushSubscription.unsubscribe().then(function(successful) {
        })['catch'](function(e) {
            // We failed to unsubscribe, this can lead to
            // an unusual state, so may be best to remove
            // the users data from your data store and
            // inform the user that you have done so

            console.log('[SW] Error while trying to unsubscribe user from notifications. ', e);
        });
      })['catch'](function(e) {
        console.error('[SW] Error while trying to unsubscribe user from notifications.', e);
      });
  });
}

function push_sendSubscriptionToServer(subscription, action) {
    var req = new XMLHttpRequest();
    var url = "/register-subscription?action" + action;
    req.open('POST', url, true);
    req.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
    req.setRequestHeader("Content-type", "application/json");
    req.onreadystatechange = function (e) {
        if (req.readyState == 4) {
            if(req.status != 200) {
                console.error("[SW] Error: " + e.target.status);
            }
        }
    };
    req.onerror = function (e) {
        console.error("[SW] Error: " + e.target.status);
    };

    var key = subscription.getKey('p256dh');
    var token = subscription.getKey('auth');

    req.send(JSON.stringify({
        'endpoint': getEndpoint(subscription),
        'key': key ? btoa(String.fromCharCode.apply(null, new Uint8Array(key))) : null,
        'token': token ? btoa(String.fromCharCode.apply(null, new Uint8Array(token))) : null
    }));

    return true;
}

function getEndpoint(pushSubscription) {
    var endpoint = pushSubscription.endpoint;
    var subscriptionId = pushSubscription.subscriptionId;

    // fix Chrome < 45
    if (subscriptionId && endpoint.indexOf(subscriptionId) === -1) {
        endpoint += '/' + subscriptionId;
    }

    return endpoint;
}
