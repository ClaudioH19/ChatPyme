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

public class Cache extends Thread{

    public Cache(){





    }


    @Override
    public void run(){
        Basededatos db = new Basededatos();

        Jedis jedis = new Jedis("redis://default:nIEkfOspjFY6EqZBRtsCPM2k2qNmvPmm@redis-10796.c9.us-east-1-4.ec2.redns.redis-cloud.com:10796");
        Connection connection = jedis.getConnection();
        while(true){

            try{


                //guardar en redis
                String[] array = HiloDeCliente.historial.toArray(new String[0]);
                // Guardar el array en Redis como una lista
                String clave = "History_Server_"+this;
                //jedis.del(clave);
                jedis.set(clave, Arrays.toString(array));

                //guardar json
                Map<String, String> collectionsJson = db.getCollectionsAsJson();
                for (Map.Entry<String, String> entry : collectionsJson.entrySet()) {
                    String key = entry.getKey();
                    String json = entry.getValue();
                    jedis.set(key, json);
                }
                Thread.sleep(60000);

            }catch (Exception e){
                System.out.println(e);
            }

        }


    }


}
