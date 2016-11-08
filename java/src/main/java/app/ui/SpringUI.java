package app.ui;

import app.interfaces.SendRequest;
import app.runnable.SendRequestsRunnable;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Ovidiu on 10/26/2016.
 */
@com.vaadin.spring.annotation.SpringUI(path = "/push-notification")
@Theme("valo")
@Push
public class SpringUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Button sendNotifications;
        TextArea jsonTextArea;
        Label progressLabel = new Label();
        Label statisticsLabel = new Label();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);


        jsonTextArea = new TextArea();
        jsonTextArea.setRows(20);
        jsonTextArea.setColumns(50);
        try {
            FileInputStream inputStream = new FileInputStream("payload.json");
            jsonTextArea.setValue(IOUtils.toString(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        jsonTextArea.sets(200, 200);


        sendNotifications = new Button("Send push-notifications");
        sendNotifications.addClickListener(event -> {
            String payload = jsonTextArea.getValue();

            try (FileWriter file = new FileWriter("payload.json")) {
                file.write(payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendNotifications.setEnabled(false);

            new Thread(new SendRequestsRunnable(new SendRequest() {
                @Override
                public void onFinish(String stats) {

                    getUI().access(() -> {
                        sendNotifications.setEnabled(true);
                        statisticsLabel.setValue(stats);
                    });
                }

                @Override
                public void onProgress(long total, long finished) {
                    access(() -> {
                        progressLabel.setValue(finished + " / " + total);
                        push();
                    });
                }
            }, payload)).start();

        });

        verticalLayout.addComponent(new Label("JSON:"));
        verticalLayout.addComponent(jsonTextArea);
        verticalLayout.addComponent(sendNotifications);
        verticalLayout.addComponent(progressLabel);
        verticalLayout.addComponent(statisticsLabel);

        setContent(verticalLayout);
    }

}
