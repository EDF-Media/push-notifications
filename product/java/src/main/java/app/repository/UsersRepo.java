package app.repository;

import app.entity.User;
import app.utility.Mongo;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ovidiu on 10/25/2016.
 */
public class UsersRepo {

    DBCursor cursor;

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        try {
            DBCollection usersTable = Mongo.getCollection("push", "users");

            System.out.println("Users counter");
            System.out.println(usersTable.count());
            System.out.println(System.currentTimeMillis());

            DBCursor cursor = usersTable.find();
            while (cursor.hasNext()) {
                User user = new Gson().fromJson(cursor.next().toString(), User.class);
                users.add(user);
            }
            System.out.println(System.currentTimeMillis());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return users;
    }

    public void uodateDeletedUser(User user) {
        try {
            DBCollection usersTable = Mongo.getCollection("push", "users");

            BasicDBObject userEntryDeleted = new BasicDBObject();
            userEntryDeleted.append("$set", new BasicDBObject().append("deleted", 1));

            BasicDBObject searchQuery = new BasicDBObject().append("token", user.getToken());
            usersTable.update(searchQuery, userEntryDeleted);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public long getSize() {
        try {
            DBCollection usersTable = Mongo.getCollection("push", "users");
            return usersTable.count();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<User> getNext(int size) {

        List<User> users = new ArrayList<>();

        if (cursor == null) {
            try {
                DBCollection usersTable = Mongo.getCollection("push", "users");
                cursor = usersTable.find();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }

        while (cursor.hasNext()) {
            User user = new Gson().fromJson(cursor.next().toString(), User.class);
            users.add(user);
            if (users.size() == size) {
                break;
            }
        }

        if (users.size() == 0) {
            return null;
        }
        return users;
    }

}
