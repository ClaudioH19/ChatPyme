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

            //se encarga de dar concordancia al estado en el server y en la base de datos
            Basededatos db = new Basededatos();
            try {
                ArrayList<String[][]> users = db.getallusers();
            }
            catch(MongoException e) {
                e.printStackTrace();
            }
            int idx=0;
            for (HiloDeCliente h : HiloDeCliente.conectados){
                try {
                    if (!db.readstatus(h.correo))
                        HiloDeCliente.conectados.remove(idx);
                }catch(MongoException e) {
                    e.printStackTrace();
                }

                idx++;
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
