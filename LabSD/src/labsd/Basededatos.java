/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package labsd;

/**
 *
 * @author claud
 */
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
public class Basededatos {

    private MongoDatabase db;
    private MongoClient mongoClient;

    public Basededatos(){
        String connectionString = "mongodb+srv://user:Password@dbchatpyme.pzrvj.mongodb.net/?retryWrites=true&w=majority&appName=DBchatpyme";
        ConnectionString connString = new ConnectionString(connectionString);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                .build();

        this.mongoClient = MongoClients.create(settings);
        this.db = mongoClient.getDatabase("db");

    }

    // CRUD,
    public void createuser(String nombre, String correo, String rut, String clave, String rol){
        try {
            MongoCollection<Document> collection = this.db.getCollection("users");
            Document newUser = new Document()
                    .append("nombre", nombre)
                    .append("correo", correo)
                    .append("rut", rut)
                    .append("clave", clave)
                    .append("rol", rol);
            collection.insertOne(newUser);
            System.out.println("Usuario creado correctamente.");
        } catch (MongoException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        }
    }

    public String[][] readuser(String c){
        MongoCollection<Document> collection = db.getCollection("users");
        Document query=collection.find(new Document("correo", c)).first();
        if (query == null) {
            System.out.println("Usuario no encontrado.");
            return null;
        }

        String nombre = query.getString("nombre");
        String correo = query.getString("correo");
        String rut = query.getString("rut");
        String clave = query.getString("clave");
        String rol = query.getString("rol");

        String[][] user = new String[5][2];
        user[0][0] = "nombre";
        user[0][1] = nombre;
        user[1][0] = "correo";
        user[1][1] = correo;
        user[2][0] = "rut";
        user[2][1] = rut;
        user[3][0] = "clave";
        user[3][1] = clave;
        user[4][0] = "rol";
        user[4][1] = rol;
        return user;
    }

    public void deleteuser(String r){
        try{
            MongoCollection<Document> collection = db.getCollection("users");
            if(collection.find(new Document("rut", r)).first() == null){
                System.out.println("Usuario no encontrado");
                return;
            }

            collection.deleteOne(new Document("rut", r));
            System.out.println("Usuario eliminado correctamente.");
        }catch (MongoException e){
            System.out.println(e);
        }

    }

    public void updateuser(String nombre, String correo, String rut, String clave, String rol){
        deleteuser(rut);
        try {
            MongoCollection<Document> collection = this.db.getCollection("users");
            Document newUser = new Document()
                    .append("nombre", nombre)
                    .append("correo", correo)
                    .append("rut", rut)
                    .append("clave", clave)
                    .append("rol", rol);
            collection.insertOne(newUser);
            System.out.println("Usuario creado correctamente.");
        } catch (MongoException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        }
    }
}
    

