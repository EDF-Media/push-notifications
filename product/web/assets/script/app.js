var isPushEnabled = true;

window.addEventListener('load', function() {
    push_subscribe();
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/service-worker.js')
        .then(function(sw) {
            push_initialiseState();
            initPostMessageListener();
        }, function (e) {});
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
    if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
        return;
    }

    if (Notification.permission === 'denied') {
        retryNotification();
        return;
    }

    if (!('PushManager' in window)) {
        return;
    }
    
    navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.getSubscription()
        .then(function(subscription) {
            if (!subscription) {
                return;
            }

            push_sendSubscriptionToServer(subscription, 'update');
            goToEscape();
        })
        ['catch'](function(err) {
            console.warn('fail getSubscription()', err);
        });
    });
}

function push_subscribe() {
    navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
        serviceWorkerRegistration.pushManager.subscribe({userVisibleOnly: true})
        .then(function(subscription) {
            console.log("Accepted");
            return push_sendSubscriptionToServer(subscription, 'create');
        })
        ['catch'](function(e) {
            if (Notification.permission === 'denied') {
                retryNotification();
            } else {
                push_subscribe();
                shake();
            }
        });
    });
}

function push_unsubscribe() {
  navigator.serviceWorker.ready.then(function(serviceWorkerRegistration) {
    serviceWorkerRegistration.pushManager.getSubscription().then(
      function(pushSubscription) {
        if (!pushSubscription) {
          return;
        }

        push_sendSubscriptionToServer(pushSubscription, 'delete');

        pushSubscription.unsubscribe().then(function(successful) {
        })['catch'](function(e) {
        });
      })['catch'](function(e) {
      });
  });
}

function push_sendSubscriptionToServer(subscription, action) {
    var req = new XMLHttpRequest();
    var url = "/api/client?action="+action;
    req.open('POST', url, true);
    req.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
    req.setRequestHeader("Content-type", "application/json");
    req.onreadystatechange = function (e) {
        if (req.readyState == 4) {
            if(req.status != 200) {}
        }
    };
    req.onerror = function (e) {
    };

    var key = subscription.getKey('p256dh');
    var token = subscription.getKey('auth');

    console.log(subscription);
    
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
