package app.runnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ovidiu on 10/24/2016.
 */
public class CallPHPRunnable implements Runnable {

    String body;
    public static final String URL = "";

    public CallPHPRunnable(String body) {
        this.body = body;
    }

    @Override
    public void run() {
        try {
            send(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        os.write(body.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }

}
