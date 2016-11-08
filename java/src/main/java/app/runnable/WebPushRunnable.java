package app.runnable;

import app.entity.User;
import app.interfaces.RequestResults;
import app.repository.UsersRepo;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class WebPushRunnable implements Runnable {

    final static Logger logger = Logger.getLogger(WebPushRunnable.class);
    User user;
    RequestResults requestResults;
    String payload;

    public WebPushRunnable(User user, RequestResults requestResults, String payload) {
        this.user = user;
        this.requestResults = requestResults;
        this.payload = payload;
    }

    @Override
    public void run() {
        Notification notification = new Notification(user.getEndpoint(), user.getUserPublicKey(), user.getToken().getBytes(),
                payload.getBytes(), 120);

        PushService pushService = new PushService();
        if (notification.isGcm()) {
            pushService.setGcmApiKey("AIzaSyBhDuJpQsKHhEb7RLJ-dc7LjmN9gpSOQJ0");
        }

        try {
            HttpResponse httpResponse = pushService.send(notification);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            String reponse = IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8);
//            EntityUtils.toString(httpResponse.getEntity());

            logger.info("Status code: " + statusCode + " " + user.getEndpoint() + " " + user.getKey() + " " + user.getToken());
            requestResults.addRequest(statusCode);

            if (statusCode / 100 != 2) {
                //not succesfull
                new UsersRepo().uodateDeletedUser(user);
            }
        } catch (NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException |
                IllegalBlockSizeException | IOException | InvalidKeySpecException | NoSuchProviderException |
                JoseException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

//        try {
//            Thread.sleep(new Random().nextInt(100));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        requestResults.addRequest(new Random().nextInt(3));
    }
}
