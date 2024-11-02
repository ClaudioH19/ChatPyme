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
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.util.ArrayList;
import java.util.*;

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

    // CRUD users,
    public void createuser(String nombre, String correo, String rut, String clave, String rol){
        try {
            MongoCollection<Document> collection = this.db.getCollection("users");
            int ingreso=0;
            Document newUser = new Document()
                    .append("nombre", nombre)
                    .append("correo", correo)
                    .append("rut", rut)
                    .append("clave", clave)
                    .append("rol", rol)
                    .append("ingreso",ingreso)
                    .append("chats", new ArrayList<>())
                    .append("status",false);

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
        int ingreso = query.getInteger("ingreso");

        String[][] user = new String[7][2];
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
        user[5][0] = "ingreso";
        user[5][1] = String.valueOf(ingreso);
        return user;
    }

    public void deleteuser(String c){
        try{
            MongoCollection<Document> collection = db.getCollection("users");
            if(collection.find(new Document("correo", c)).first() == null){
                System.out.println("Usuario no encontrado");
                return;
            }

            collection.deleteOne(new Document("correo", c));
            System.out.println("Usuario eliminado correctamente.");
        }catch (MongoException e){
            System.out.println(e);
        }

    }

    public void updateuser(String c,String parametros,String key) {
        try {
            MongoCollection collection = db.getCollection("users");
            collection.findOneAndUpdate(new Document("correo",c), Updates.set(parametros,key));
            System.out.println("Usuario actualizado correctamente.");
        } catch (MongoException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
        }
    }

    public ArrayList getchat(String c){
        MongoCollection<Document> collection = this.db.getCollection("users");
        Document query=collection.find(new Document("correo", c)).first();
        return query.get("chats", ArrayList.class);
    }

    public void addmensajes(String c,String msg){
        MongoCollection<Document> collection = this.db.getCollection("users");
        collection.updateOne(new Document("correo", c), Updates.push("chats",msg));
    }

    public void dropmensajes(String c){
        MongoCollection<Document> collection = this.db.getCollection("users");
        collection.updateOne(new Document("correo", c), Updates.set("chats", new ArrayList<>()));
    }

    public void changestatus(String c,boolean estado){
        MongoCollection<Document> collection = this.db.getCollection("users");
        collection.updateOne(new Document("correo",c),Updates.set("status",estado));
    }

    public boolean readstatus(String c){
        MongoCollection<Document> collection = this.db.getCollection("users");
        return Objects.requireNonNull(collection.find(new Document("correo", c)).first()).getBoolean("status");
    }

    public int readingreso(String c){
        MongoCollection<Document> collection = this.db.getCollection("users");
        return collection.find(new Document("correo",c)).first().getInteger(("ingreso"));
    }

    public void incrementingreso(String c){
        MongoCollection<Document> collection = this.db.getCollection("users");
        collection.findOneAndUpdate(new Document("correo",c),Updates.inc("ingreso",1));
    }

    public ArrayList<String[][]> getallusers(){
        ArrayList<String[][]> users = new ArrayList <String[][]>();
        MongoCollection<Document> collection = db.getCollection("users");
        MongoCursor<Document> cursor = collection.find().iterator();
        while(cursor.hasNext()){
            Document user = cursor.next();
            users.add(readuser(user.get("correo").toString()));
        }

        return users;
    }


    //CRUD groups
    public void creategroup(String nombre, ArrayList<String> Integrantes){
        try{
            MongoCollection<Document> collection = db.getCollection("groups");
            ArrayList<String> mensajes = new ArrayList<>();
            Document newGroup = new Document()
                    .append("nombre", nombre)
                    .append("integrantes", Integrantes)
                    .append("mensajes",mensajes);
        }catch (MongoException e){
            System.err.println("Error al insertar group: " + e.getMessage());
        }
    }

    public void deletegroup(String g){
            MongoCollection<Document> collection = db.getCollection("groups");
            collection.deleteOne(new Document("nombre", g));
    }

    public void insertmsg(String nombre, String mensaje){
        MongoCollection<Document> collection = db.getCollection("groups");
        collection.updateOne(new Document("nombre", nombre), Updates.push("mensajes",mensaje));
    }

    public void insertuser(String nombre, String user){
        MongoCollection<Document> collection = db.getCollection("groups");
        collection.updateOne(new Document("nombre", nombre), Updates.push("integrantes",user));
    }

    public void deleteuser(String nombre, String user){
        MongoCollection<Document> collection = db.getCollection("groups");
        collection.updateOne(new Document("nombre", nombre), Updates.push("integrantes",user));
    }

    public void deletemsg(String nombre){
        MongoCollection<Document> collection = db.getCollection("groups");
        collection.updateOne(new Document("nombre", nombre), Updates.popLast("mensajes"));
    }



}
    

