package app.runnable;

import app.entity.User;
import app.interfaces.RequestResults;
import app.interfaces.SendRequest;
import app.repository.UsersRepo;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class SendRequestsRunnable implements Runnable, RequestResults {

    public static final int NR_THREADS = 10;

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    BlockingQueue<User> queue = new LinkedBlockingQueue<>(100);

    final static Logger logger = Logger.getLogger(SendRequestsRunnable.class);

    SendRequest sendRequest;
    String payload;

    public SendRequestsRunnable(SendRequest sendRequest, String payload) {
        this.sendRequest = sendRequest;
        this.payload = payload;
    }

    @Override
    public void run() {
        logger.info("Start time: " + dateFormat.format(new Date()));
        long startTime = System.currentTimeMillis();

        new Thread(new GetUsersRunnable(queue)).start();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(NR_THREADS, NR_THREADS,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

        long totalUsers = new UsersRepo().getSize();
        long time = System.currentTimeMillis();
        sendRequest.onProgress(totalUsers, totalSendRequest);

        while (true) {
            try {
                long currentTime = System.currentTimeMillis();
                if (currentTime - time > 1000) {
                    time = currentTime;
                    sendRequest.onProgress(totalUsers, totalSendRequest);
                }

                logger.debug("Queue size consumer " + queue.size());
                User user = queue.poll(10, TimeUnit.SECONDS);
                if (user == null) {
                    //queue is empty
                    break;
                }

                executor.execute(new WebPushRunnable(user, this, payload));
                while (executor.getQueue().size() > 10) {
                    logger.debug("Thread pool current queue size: " + executor.getQueue().size() + " waiting for execution");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        logger.info("Shutdown executor");
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Waiting threads to finish");
        }

        sendRequest.onProgress(totalUsers, totalSendRequest);

        int total = 0;
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            total +=  entry.getValue();
        }


        String stats = writeStats(System.currentTimeMillis() - startTime, total, countMap);
        sendRequest.onFinish(stats);

        logger.info("Finish time: " + dateFormat.format(new Date()));
    }

    private String writeStats(long timeMS, long counter, Map<Integer, Integer> statusCodeCounter) {
        StringBuilder statistics = new StringBuilder();
        statistics.append("Payload: ").append("\n").append(payload).append(System.lineSeparator());
        statistics.append("Time: ").append(timeMS / 1000).append(" s").append(System.lineSeparator());
        statistics.append("Total sent requests: ").append(counter).append(System.lineSeparator());
        for (Map.Entry<Integer, Integer> status : statusCodeCounter.entrySet()) {
            statistics.append("Status code: ").append(status.getKey()).append(" Count: ").append(status.getValue()).append(System.lineSeparator());
        }

        try (FileWriter file = new FileWriter("statistics/" + new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()) + ".txt")) {
            file.write(statistics.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return statistics.toString();
    }

    Map<Integer, Integer> countMap = new HashMap<>();
    int totalSendRequest = 0;

    @Override
    public void addRequest(int statusCode) {
        synchronized (SendRequestsRunnable.this) {
            ++totalSendRequest;
            if (countMap.containsKey(statusCode)) {
                countMap.put(statusCode, countMap.get(statusCode) + 1);
            } else {
                countMap.put(statusCode, 1);
            }
        }
    }
}
