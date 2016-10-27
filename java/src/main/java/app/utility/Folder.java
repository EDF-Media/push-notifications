package app.utility;

import java.io.File;

/**
 * Created by Ovidiu on 10/27/2016.
 */
public class Folder {

    public static boolean create(String name) {
        File theDir = new File(name);
        if (!theDir.exists()) {
            return theDir.mkdir();
        }
        return true;
    }

}
