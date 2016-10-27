package app.controller;

import app.runnable.SendRequestsRunnable;
import app.utility.RedisService;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class PushController {

    final static Logger logger = Logger.getLogger(PushController.class);

    @RequestMapping(value = "/virus-notification", method = RequestMethod.GET)
    public String sendPushNotifications() {
        try {
            FileInputStream inputStream = new FileInputStream("payload.json");
            return IOUtils.toString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
