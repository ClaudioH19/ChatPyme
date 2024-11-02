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
        Basededatos db = new Basededatos();
        Jedis jedis = new Jedis("redis://default:TSf6GFtmMP83H4Zuzri1wD2HbYmktgRZ@redis-18532.c91.us-east-1-3.ec2.redns.redis-cloud.com:18532");
        Connection connection = jedis.getConnection();
        while(true){

            try{
                Thread.sleep(60000);

                //guardar en redis
                String[] array = HiloDeCliente.historial.toArray(new String[0]);
                // Guardar el array en Redis como una lista
                String clave = "history"+System.currentTimeMillis();
                //jedis.del(clave);
                jedis.rpush(clave, array);

                //guardar json
                Map<String, String> collectionsJson = db.getCollectionsAsJson();
                for (Map.Entry<String, String> entry : collectionsJson.entrySet()) {
                    String key = entry.getKey();
                    String json = entry.getValue();
                    jedis.set(key, json);
                }


            }catch (Exception e){
                System.out.println(e);
            }

        }


    }
}
