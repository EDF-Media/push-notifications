package app.runnable;

import app.entity.User;
import app.repository.UsersRepo;
import nl.martijndwars.webpush.Notification;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class GetUsersRunnable implements Runnable {

    final static Logger logger = Logger.getLogger(GetUsersRunnable.class);
    public static final int RETRIEVE_USERS_SIZE = 10;
    BlockingQueue<User> queue;

    public GetUsersRunnable(BlockingQueue<User> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        UsersRepo usersRepo = new UsersRepo();
        while (true) {
            List<User> users = usersRepo.getNext(RETRIEVE_USERS_SIZE);
            if (users == null) {
                break;
            }

            for (User user : users) {
                try {
                    logger.debug("Queue size producer " + queue.size());
                    queue.put(user);
//                    queue.put(testUser());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private User testUser() {
        String endpoint = "https://android.googleapis.com/gcm/send/fXNwI1o_lcw:APA91bHiOVwnvIXcCWk4AlesLa04wF4mfnoiNWf_s9YD3mZ_CQMn8y8dCuk22rm91wT9c1hb4gUkbx8Od6U3UyWo44M82kI5f6vlLXozXzfkeYQfasDaFgGjGQgrH5xBYb8v3XRBinfi";
        String publicKey = "BNy8PudOUZ9vPdqeFgcbzwRccG3Qbb8jFjd/4btIFt2RU6eahcgdqk9kvmFcVaxDC+ehsAcryUsHyaog233UL4I=";
        String authToken = "8EXAvdT8sRhIKR4bqpXwcQ==";
        String payload = "";
        int ttl = 120;

        User user = new User();
        user.setEndpoint(endpoint);
        user.setKey(publicKey);
        user.setToken(authToken);
        return user;
    }
}
