package labsd;

import com.mongodb.MongoException;

import java.util.ArrayList;

public class Comandos {

    HiloDeCliente h;
    public static ArrayList<String[][]> users;
    public Comandos(HiloDeCliente h) {
        this.h = h;
    }

    public void comandos(String texto, String[] textsplitted, String idDest, String origin) throws Throwable {


        //PRIVATE MSG
        //###############################################################
        if (texto.contains("/private") && validationrol()) {

            String auxtext = "";
            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/private")) {
                    idDest = textsplitted[i + 1];
                    for (int j = i + 2; j < textsplitted.length; j++) {
                        auxtext += " " + textsplitted[j];
                    }
                    break;
                }
            }



            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(idDest) || h.correo.equalsIgnoreCase(idDest) || h.nombre.equalsIgnoreCase(idDest)) {
                    h.reenviarAlmismosocket("#blue#[PRIVATE FROM ~" + this.h.nombre + " ~]: " + "#black#"+auxtext);
                    break;
                }
            }

            //obtener nombre de origin
            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(idDest) || h.correo.equalsIgnoreCase(idDest) || h.nombre.equalsIgnoreCase(idDest)) {
                    idDest = h.nombre;
                }
            }

            //buscar destinatario si esta offline
            for (String[][] s: users) {
                System.out.println(s[0][1]+" "+s[1][1]);
                if ((s[0][1].equals(idDest) || s[1][1].equals(idDest) ) && !this.h.db.readstatus(s[1][1]) ) {
                    this.h.db.addmensajes(s[1][1], "#blue#[PRIVATE FROM ~" + this.h.nombre + " ~]: " + "#black#" + auxtext);
                    break;
                }
            }

            this.h.reenviarAlmismosocket("#blue#[PRIVATE TO ~" + idDest + "~]: " + "#black#"+auxtext);


            //CREATE GROUP
            //###############################################################
        } else if (texto.contains("/create group") && validationrol()) {
            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/create") && textsplitted[i + 1].equals("group")) {

                    //guardar en MONGO
                    ArrayList<String> integrantes= new ArrayList();
                    integrantes.add(this.h.correo);
                    this.h.db.creategroup(textsplitted[i + 2],integrantes);

                    this.h.reenviarAlmismosocket("#green#GRUPO " + textsplitted[i + 2] + " CREADO\n");
                    HiloDeCliente.groups= this.h.db.getallgroups();
                }

            }
            //INFO DEL USUARIO
            //###############################################################
        } else if (texto.contains("/whoami") && validationrol()) {
            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(origin)) {
                    h.reenviarAlmismosocket("#blue#CONNECTED AS " + h.nombre+String.format(" <%s> ",this.h.rol) + "\n");
                }
            }
            //UNIRSE A UN GRUPO
            //###############################################################
        } else if (texto.contains("/join group") && validationrol()) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/join") && textsplitted[i + 1].equals("group")) {


                    ArrayList<String >integrantes = this.h.db.getintegrantes(textsplitted[i + 2]);
                    //UNIRSE EN MONGO
                     this.h.db.insertuser(textsplitted[i + 2],this.h.correo);

                     //reenviar a todos los del grupo
                        for (int k = 0; k < integrantes.size(); k++) {
                            for (HiloDeCliente h : HiloDeCliente.conectados) {
                                if(h.correo.equalsIgnoreCase(integrantes.get(k)))
                                        h.reenviarAlmismosocket("#green#"+this.h.nombre +" SE HA UNIDO AL GRUPO " + textsplitted[i+2]);
                                }
                            }
                            this.h.reenviarAlmismosocket("#green#UNIDO A GRUPO " + textsplitted[i+2]);
                            HiloDeCliente.groups= this.h.db.getallgroups();
                            break;

                }

            }
            // MENSAJE GRUPO
            //###############################################################
        } else if (texto.contains("/group") && validationrol()) {

            String auxtext = "";
            for (int i = 0; i < textsplitted.length; i++) {

                if (textsplitted[i].equals("/group")) {

                    for (int j = i + 2; j < textsplitted.length; j++) {
                        auxtext += " " + textsplitted[j];
                    }


                    ArrayList<String> integrantes = this.h.db.getintegrantes(textsplitted[i + 1]);


                            boolean entrar = false;

                            //validar que existe en ese grupo
                            for (int k = 0; k < integrantes.size(); k++) {
                                if (this.h.correo.equals(integrantes.get(k))) {
                                    entrar = true;
                                    break;
                                }
                            }

                            //obtener nombre de origin
                            for (HiloDeCliente h : HiloDeCliente.conectados) {
                                if (h.idserver.equalsIgnoreCase(origin) || h.correo.equalsIgnoreCase(origin) || h.nombre.equalsIgnoreCase(origin)) {
                                    origin = h.nombre;
                                }
                            }

                            //guardar mensaje en MONGO
                            this.h.db.insertmsg(textsplitted[i + 1],"#blue#[FROM GROUP ~" + textsplitted[i + 1] + "~ BY ~" + origin + "~]: " +"#black#"+auxtext);

                            for (int k = 0; entrar && k < integrantes.size(); k++) {
                                for (HiloDeCliente h : HiloDeCliente.conectados) {
                                    if (h.correo.equalsIgnoreCase(integrantes.get(k))) {
                                        h.reenviarAlmismosocket("#blue#[FROM GROUP ~" + textsplitted[i + 1] + "~ BY ~" + origin + "~]: " +"#black#"+auxtext);
                                    }
                                }
                            }
                            ArrayList<String> mensajes= this.h.db.getmensajes(textsplitted[i + 1]);
                            if(!mensajes.isEmpty()){
                                this.h.reenviarAlmismosocket("#orange#Use ~/msgs <group>~ para ver los mensajes del grupo");
                            }
                            break;
                }

            }
            //cargar mensajes del grupo
        } else if(texto.contains("/msgs") && validationrol()) {
            for (int i=0; i<textsplitted.length; i++) {
                if (textsplitted[i].equals("/msgs")) {
                    ArrayList<String> mensajes= this.h.db.getmensajes(textsplitted[i + 1]);
                    for (String s : mensajes) {
                        this.h.reenviarAlmismosocket(s);
                    }
                }
            }
        }//GUIA DE AYUDA DE COMANDOS
        //###############################################################
        else if (texto.contains("/help") && validationrol()) {

            this.h.reenviarAlmismosocket(""
                    + "/whoami ----------> Muestra el ID del server \n"
                    + "/private # --------> Envía un mensaje privado al ID # \n"
                    + "/allgroups --------> Muestra todos los gropos creados \n"
                    + "/group # ----------> Envía un mensaje en el grupo # \n"
                    + "/create group # --> Crea un grupo de nombre # \n"
                    + "/join group # -----> Se une al grupo de nombre # \n"
                    + "/leave group # ---> Se va del grupo de nombre # \n"
                    + "/status ------------> Muestra el estado del servidor\n\n"
            );
            // ABANDONAR GRUPO
            //###############################################################
        } else if (texto.contains("/leave group") && validationrol()) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/leave") && textsplitted[i + 1].equals("group")) {

                    ArrayList<String> integrantes = this.h.db.getintegrantes(textsplitted[i + 2]);


                    for (int k = 0; k < integrantes.size(); k++) {
                        if (this.h.correo.equalsIgnoreCase(integrantes.get(k))) {
                            this.h.reenviarAlmismosocket("#red#GRUPO " + textsplitted[i + 2] + " ABANDONADO\n");
                            integrantes.remove(k);
                            this.h.db.deleteusergroup(textsplitted[i + 2],this.h.correo);
                            break;
                        }
                    }
                }

            }
            HiloDeCliente.groups= this.h.db.getallgroups();
            // LISTAR GRUPOS
            //###############################################################
        } else if (texto.contains("/allgroups") && validationrol()) {
            this.h.reenviarAlmismosocket("#blue#~GRUPOS CREADOS~:\n---------------------");
            ArrayList<ArrayList> grupos = this.h.db.getallgroups();
            for (int i = 0; i < grupos.size(); i++) {

                System.out.println(grupos.get(i));

                this.h.reenviarAlmismosocket("#blue#~NOMBRE GRUPO:~ " + grupos.get(i).get(0) + "\n~ Integrantes:~");

                ArrayList<String> integrantes = (ArrayList<String>) grupos.get(i).get(1);
                for (int j = 0; j < integrantes.size(); j++){
                    this.h.reenviarAlmismosocket("  >#blue#" + integrantes.get(j) + "\n\n");
                }
            }
            //VER CONTECTADOS
            //###############################################################
        } else if (texto.contains("/status") && validationrol()) {
            this.h.reenviarAlmismosocket("#green#~ONLINE:~ \n---------------------");
            for (int i = 0; i < HiloDeCliente.conectados.size(); i++) {

                String nombre="";
                String rol="";
                String correo="";
                //obtener nombre
                for(HiloDeCliente h : HiloDeCliente.conectados) {
                    if(h.idserver.equalsIgnoreCase(String.valueOf(HiloDeCliente.conectados.get(i).idserver))) {
                        nombre=h.nombre;
                        rol=h.rol;
                        correo=h.correo;
                    }
                }

                this.h.reenviarAlmismosocket(String.format("#blue#User: %s %s <%s> IS #green#CONNECTED\n---------------------",nombre,correo,rol));
            }
        }
        //LOGUEARSE
        //###############################################################
        //formato correo clave
        else if(texto.contains("/login")){
            if(this.h.rol!="") {this.h.reenviarAlmismosocket("#red#CIERRE SESIÓN PRIMERO ~/logout~"); return;};
            for (int i = 0; i < textsplitted.length; i++) {
                if(textsplitted[i].equals("/login")){
                    String correo=textsplitted[i+1];
                    String clave=textsplitted[i+2];
                    String[][] user=h.db.readuser(correo);
                    if(user==null){
                        this.h.reenviarAlmismosocket("#red#USUARIO NO ENCONTRADO");
                        return;
                    }

                    if(!user[3][1].equals(clave)){
                        this.h.reenviarAlmismosocket("#red#CONTRASEÑA INCORRECTA");
                        return;
                    }
                    this.h.nombre=user[0][1];
                    this.h.correo=user[1][1];
                    this.h.rut=user[2][1];
                    this.h.clave=user[3][1];
                    this.h.rol=user[4][1];
                    this.h.ingreso=Integer.parseInt(user[5][1]);
                    this.h.reenviarAlmismosocket("~#blue#LOGUEADO COMO:~ "+this.h.nombre+" <"+this.h.rol+">");
                    if(this.h.ingreso>=1)
                        this.h.ingreso+=1;

                    if(this.h.ingreso<1){
                        this.h.reenviarAlmismosocket("#red#Use ~/newpassword <clave>~ PARA CAMBIAR SU CONTRASEÑA");
                    }

                    this.h.db.incrementingreso(this.h.correo);
                    this.h.db.changestatus(this.h.correo,true);
                    // Guardar el HiloDeCliente y enviar el ID al cliente
                    HiloDeCliente.conectados.add(this.h);

                    for (int k = 0; k < HiloDeCliente.conectados.size(); k++) {
                        HiloDeCliente.conectados.get(k).reenviarAlmismosocket(
                                "#blue#"+this.h.nombre+String.format(" ~<%s>~ ",this.h.rol) + " Connected");
                    }


                    //recuperar mensajes en offline
                    if(!this.h.db.getchat(this.h.correo).isEmpty()){
                        this.h.reenviarAlmismosocket("#orange#Hay Mensajes pendientes, escriba ~/read~ para visualizar.");
                    }

                    this.users=this.h.db.getallusers();
                    break;
                }

            }

        }else if(texto.contains("/newpassword")){
            for (int i = 0; i < textsplitted.length; i++) {
                if(textsplitted[i].equals("/newpassword") && this.h.ingreso<=1){
                    this.h.clave=textsplitted[i+1];
                    this.h.ingreso+=1;
                    this.h.db.updateuser(this.h.correo,"clave",this.h.clave);
                    this.h.reenviarAlmismosocket("#BLUE# ~CONTRASEÑA MODIFICADA~");
                }
            }
        }else if(texto.contains("/read") && validationrol()){
            this.h.reenviarAlmismosocket("#orange#~leyendo...~");
            ArrayList mensajes = this.h.db.getchat(this.h.correo);
            for (int i = 0; i < mensajes.size(); i++) {
                this.h.reenviarAlmismosocket(String.valueOf(mensajes.get(i)));
            }
            this.h.db.dropmensajes(this.h.correo);
        }
        else if(texto.contains("/register")){
            if(!this.h.rol.equals("administrador")){
                this.h.reenviarAlmismosocket("#red#SOLO ~ADMINISTRADORES~ PUEDEN REGISTRAR");
                return;
            }
            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/register")) {
                    String nombre = textsplitted[i + 1];
                    String correo = textsplitted[i + 2];
                    String rut = textsplitted[i + 3];
                    String clave = textsplitted[i + 4];
                    String rol = textsplitted[i + 5];
                    try {
                        this.h.db.createuser(nombre, correo, rut, clave, rol);
                        this.h.reenviarAlmismosocket("#blue#USUARIO "+ nombre +" CREADO");
                    } catch (MongoException e) {
                        this.h.reenviarAlmismosocket(String.valueOf(e));
                    }
                }
            }
        }//EXIT
        //###############################################################
        else if (texto.contains("/exit")) {
            this.h.db.changestatus(this.h.correo,false);
            for (int i = 0; i < HiloDeCliente.conectados.size(); i++) {
                HiloDeCliente.conectados.get(i).reenviarAlmismosocket("#red#" + this.h.nombre + " IS DISCONNECTED\n---------------------");
                if (HiloDeCliente.conectados.get(i).idserver.equals(origin)) {
                    HiloDeCliente.conectados.get(i).connected = false;
                }
            }

        }
    }



    public boolean validationrol(){
        if(this.h.ingreso==0){
            this.h.reenviarAlmismosocket("#red#NECESITA UNA NUEVA CONTRASEÑA, USE ~/newpassword <clave>~");
            return false;
        }
        System.out.println(this.h.rol);
        if(this.h.rol==null || this.h.rol=="null" || this.h.rol==""){
            this.h.reenviarAlmismosocket("#blue#NECESITA INGRESAR AL SISTEMA.");
            return false;
        }
        return true;
    }




}
