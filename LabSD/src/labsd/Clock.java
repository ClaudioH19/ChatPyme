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

        while (true) {
            Basededatos db = new Basededatos();
            Iterator<HiloDeCliente> iterator = HiloDeCliente.conectados.iterator();

            while (iterator.hasNext()) {
                HiloDeCliente h = iterator.next();
                try {
                    if (!db.readstatus(h.correo)) {
                        iterator.remove();
                    }
                } catch (MongoException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
