package app.utility;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class ApplicationProperties {

    final static Logger logger = Logger.getLogger(ApplicationProperties.class);

    public static final String  PROP_FILE_NAME = "application.properties";

    private static Properties prop;

    private static Properties getPropFile() {
        if (prop == null) {
            prop = new Properties();

            InputStream inputStream = ApplicationProperties.class.getClassLoader().getResourceAsStream(PROP_FILE_NAME);
            try {
                prop.load(inputStream);
            } catch (IOException e) {
                logger.error("File not found: " + PROP_FILE_NAME);
                e.printStackTrace();
            }
        }
        return prop;
    }

    public static String getValue(String key) {
        return getPropFile().getProperty(key);
    }

    public static Integer getValueInt(String key) {
        return Integer.valueOf(getPropFile().getProperty(key));
    }

}
