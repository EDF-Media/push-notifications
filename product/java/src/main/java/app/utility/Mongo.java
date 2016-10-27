package app.utility;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class Mongo {

    public static DBCollection getCollection(String db, String collection) throws UnknownHostException {
        return new MongoClient("198.15.99.131").getDB(db).getCollection(collection);
    }

}
