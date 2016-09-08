package org.surfing.service.persistence;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.surfing.service.mqtt.MQTTController;

/**
 *
 * @author vsenger
 */
@Path("/persistence")
public class Persistence extends MQTTController {

    static MongoClient mongoClient;
    static DB db;
    DBCollection coll;

    @GET
    @Produces("text/html")
    @Path("/list/{collection}")
    public String update(@PathParam("collection") String collection) {
        DBCursor cursor = db.getCollection(collection).find();
        String lazyReturn = "<html><body>";
        try {
            while (cursor.hasNext()) {
                lazyReturn += "<p>" + cursor.next() + "</p>";
            }
        } finally {
            cursor.close();
        }
        lazyReturn += "</body></html>";
        return lazyReturn;
    }

    public static void save(String json, String collectionName) {
        try {
            DBCollection collection = db.getCollection(collectionName);

            // convert JSON to DBObject directly
            DBObject dbObject = (DBObject) JSON
                    .parse(json);

            collection.insert(dbObject);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void save(String key, String value, Date timestamp, String collection) {
        BasicDBObject doc = new BasicDBObject(key, value)
                .append("timestamp", timestamp.toString());
        db.getCollection(collection).insert(doc);
    }

    public static String DB_NAME = "surfing";
    public static String DB_HOST = "localhost";
    public static String DB_PORT = "";
    public static String DB_COLLECTION = "";

    @Override
    public void run() {
    }

    @Override
    public void start() {

        if (getConfig().getProperty("database.name") != null) {
            DB_NAME = getConfig().getProperty("database.name");
        }
        if (getConfig().getProperty("database.host") != null) {
            DB_HOST = getConfig().getProperty("database.host");
        }
        if (getConfig().getProperty("database.collection") != null) {
            DB_COLLECTION = getConfig().getProperty("database.collection");
        }
        if (getConfig().getProperty("database.host.port") != null) {
            DB_PORT = getConfig().getProperty("database.host.port");
        }
        try {
            mongoClient = new MongoClient(DB_HOST);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Persistence.class.getName()).log(Level.SEVERE, "Critical error to communicate with MongoDB at " + DB_HOST, ex);
        }
        db = mongoClient.getDB(DB_NAME);

    }

    @Override
    public void processMessage(String msg) {
        Logger.getLogger(Persistence.class.getName()).log(Level.INFO, "Saving data on " + DB_HOST);

        save(msg, DB_COLLECTION);
    }

    @Override
    public void stop() {
        mongoClient.close();
    }

}
