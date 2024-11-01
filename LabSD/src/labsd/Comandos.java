package labsd;

import com.mongodb.MongoException;

import java.util.ArrayList;

public class Comandos {

    HiloDeCliente h;

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
                    h.reenviarAlmismosocket("[PRIVATE FROM " + this.h.nombre + "]: " + auxtext);
                    break;
                }
            }
            //obtener nombre de origin
            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(idDest) || h.correo.equalsIgnoreCase(idDest) || h.nombre.equalsIgnoreCase(idDest)) {
                    idDest = h.nombre;
                }
            }
            this.h.reenviarAlmismosocket("[PRIVATE TO " + idDest + "]: " + auxtext);


            //CREATE GROUP
            //###############################################################
        } else if (texto.contains("/create group") && validationrol()) {
            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/create") && textsplitted[i + 1].equals("group")) {
                    ArrayList<String> g = new ArrayList();
                    g.add(textsplitted[i + 2]);
                    g.add(origin);

                    HiloDeCliente.groups.add(g);
                    this.h.reenviarAlmismosocket("GRUPO " + textsplitted[i + 2] + " CREADO\n");
                }

            }
            //INFO DEL USUARIO
            //###############################################################
        } else if (texto.contains("/whoami") && validationrol()) {
            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(origin)) {
                    h.reenviarAlmismosocket("CONNECTED AS " + h.nombre+String.format(" <%s> ",this.h.rol) + "\n");
                }
            }
            //UNIRSE A UN GRUPO
            //###############################################################
        } else if (texto.contains("/join group") && validationrol()) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/join") && textsplitted[i + 1].equals("group")) {

                    for (int j = 0; j < HiloDeCliente.groups.size(); j++) {
                        ArrayList auxg = HiloDeCliente.groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 2])) {
                            auxg.add(this.h.idserver);


                            //reenviar a todos los del grupo
                            for (int k = 0; k < auxg.size(); k++) {
                                for (int l = 0; l < HiloDeCliente.conectados.size(); l++) {
                                    if(HiloDeCliente.conectados.get(l).idserver.equalsIgnoreCase(String.valueOf(auxg.get(k))))
                                        HiloDeCliente.conectados.get(l).reenviarAlmismosocket(this.h.nombre +" SE HA UNIDO AL GRUPO " + auxg.get(0));
                                }
                            }
                            //this.reenviarAlmismosocket("UNIDO A GRUPO " + auxg.get(0));
                            break;
                        }
                    }
                }

            }
            // CREAR GRUPO
            //###############################################################
        } else if (texto.contains("/group") && validationrol()) {

            String auxtext = "";
            for (int i = 0; i < textsplitted.length; i++) {

                if (textsplitted[i].equals("/group")) {

                    for (int j = i + 2; j < textsplitted.length; j++) {
                        auxtext += " " + textsplitted[j];
                    }

                    for (int j = 0; j < HiloDeCliente.groups.size(); j++) {
                        ArrayList auxg = HiloDeCliente.groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 1])) {

                            boolean entrar = false;

                            //validar que existe en ese grupo
                            for (int k = 1; k < auxg.size(); k++) {
                                if (origin.equals(String.valueOf(auxg.get(k)))) {
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


                            for (int k = 1; entrar && k < auxg.size(); k++) {
                                for (HiloDeCliente h : HiloDeCliente.conectados) {
                                    if (h.idserver.equalsIgnoreCase(String.valueOf(auxg.get(k)))) {
                                        h.reenviarAlmismosocket("[FROM GROUP " + textsplitted[i + 1] + " BY " + origin + "]: " + auxtext);
                                    }
                                }
                            }
                            break;
                        }

                    }

                }

            }
            //GUIA DE AYUDA DE COMANDOS
            //###############################################################
        } else if (texto.contains("/help") && validationrol()) {

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

                    for (int j = 0; j < HiloDeCliente.groups.size(); j++) {
                        ArrayList auxg = HiloDeCliente.groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 2])) {

                            for (int k = 1; k < auxg.size(); k++) {
                                if (this.h.idserver.equalsIgnoreCase(String.valueOf(auxg.get(k)))) {
                                    this.h.reenviarAlmismosocket("GRUPO " + textsplitted[i + 2] + " ABANDONADO\n");
                                    auxg.remove(k);
                                    break;
                                }
                            }

                        }
                    }
                }

            }
            // LISTAR GRUPOS
            //###############################################################
        } else if (texto.contains("/allgroups") && validationrol()) {
            this.h.reenviarAlmismosocket("GRUPOS CREADOS:\n---------------------");
            for (int i = 0; i < HiloDeCliente.groups.size(); i++) {
                ArrayList auxg = HiloDeCliente.groups.get(i);
                this.h.reenviarAlmismosocket("    NOMBRE GRUPO: " + auxg.get(0) + "\n Integrantes:");


                for (int j = 1; j < auxg.size(); j++) {

                    String nombre="";
                    //obtener nombre
                    for(HiloDeCliente h : HiloDeCliente.conectados) {
                        if(h.idserver.equalsIgnoreCase(String.valueOf(auxg.get(j)))) {
                            nombre=h.nombre;
                        }
                    }

                    this.h.reenviarAlmismosocket("    ID: " + nombre + "\n\n");
                }
            }
            //VER CONTECTADOS
            //###############################################################
        } else if (texto.contains("/status") && validationrol()) {
            this.h.reenviarAlmismosocket("ONLINE: \n---------------------");
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

                this.h.reenviarAlmismosocket(String.format("ID: %s %s <%s> IS CONNECTED\n---------------------",nombre,correo,rol));
            }
        }
        //LOGUEARSE
        //###############################################################
        //formato correo clave
        else if(texto.contains("/login")){
            for (int i = 0; i < textsplitted.length; i++) {
                if(textsplitted[i].equals("/login")){
                    String correo=textsplitted[i+1];
                    String clave=textsplitted[i+2];
                    String[][] user=h.db.readuser(correo);
                    if(user==null){
                        this.h.reenviarAlmismosocket("USUARIO NO ENCONTRADO");
                        return;
                    }

                    if(!user[3][1].equals(clave)){
                        this.h.reenviarAlmismosocket("CONTRASEÑA INCORRECTA");
                        return;
                    }
                    this.h.nombre=user[0][1];
                    this.h.correo=user[1][1];
                    this.h.rut=user[2][1];
                    this.h.clave=user[3][1];
                    this.h.rol=user[4][1];
                    this.h.reenviarAlmismosocket("LOGUEADO COMO: "+this.h.nombre+" <"+this.h.rol+">");

                    // Guardar el HiloDeCliente y enviar el ID al cliente
                    HiloDeCliente.conectados.add(this.h);

                    for (int k = 0; k < HiloDeCliente.conectados.size(); k++) {
                        HiloDeCliente.conectados.get(k).reenviarAlmismosocket(
                                this.h.nombre+String.format("<%s> ",this.h.rol) + " Connected");
                    }
                    break;
                }

            }

        }
        else if(texto.contains("/register")){
            if(!this.h.rol.equals("administrador")){
                this.h.reenviarAlmismosocket("SOLO ADMINISTRADORES PUEDEN REGISTRAR");
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
                        this.h.reenviarAlmismosocket("USUARIO "+ nombre +" CREADO");
                    } catch (MongoException e) {
                        this.h.reenviarAlmismosocket(String.valueOf(e));
                    }
                }
            }
        }//EXIT
        //###############################################################
        else if (texto.contains("/exit")) {
            for (int i = 0; i < HiloDeCliente.conectados.size(); i++) {
                HiloDeCliente.conectados.get(i).reenviarAlmismosocket("ID: " + this.h.nombre + " IS DISCONNECTED\n---------------------");
                if (HiloDeCliente.conectados.get(i).idserver.equals(origin)) {
                    HiloDeCliente.conectados.get(i).connected = false;
                }
            }

        }
    }



    public boolean validationrol(){
        System.out.println(this.h.rol);
        if(this.h.rol==null || this.h.rol=="null"){
            this.h.reenviarAlmismosocket("NECESITA INGRESAR AL SISTEMA.");
            return false;
        }
        return true;
    }




}
