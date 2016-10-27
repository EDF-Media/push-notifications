package app.interfaces;

import java.util.Map;

/**
 * Created by Ovidiu on 10/26/2016.
 */
public interface SendRequest {
    void onFinish(String result);

    void onProgress(long total, long finished);
}
