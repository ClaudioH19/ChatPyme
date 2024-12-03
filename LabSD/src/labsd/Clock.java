package labsd;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Connection;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.*;
public class Clock extends Thread{

    @Override
    public void run(){

        while(true) {
        }
    }
}
