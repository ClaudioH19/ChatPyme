package labsd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class HiloDeCliente implements Runnable, ListDataListener {

    static public ArrayList<HiloDeCliente> conectados;
    static public ArrayList<ArrayList> groups;
    private DefaultListModel mensajes;
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;

    private String nombre;
    private String correo;
    private String rut;
    private String clave;
    private String rol;

    public String idserver;
    public boolean connected;
    public Basededatos db;

    public HiloDeCliente(DefaultListModel mensajes, Socket socket) {
        this.idserver = "";
        this.mensajes = mensajes;
        this.socket = socket;
        this.connected = true;
        this.db = new Basededatos();

        try {
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            mensajes.addListDataListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        this.idserver = Thread.currentThread().getName();

        for (int i = 0; i < HiloDeCliente.conectados.size(); i++) {
            HiloDeCliente.conectados.get(i).reenviarAlmismosocket(
                    this.idserver + " Connected");
        }

        try {
            while (connected) {
                String texto = dataInput.readUTF();
                String[] textsplitted = texto.split(" ");
                String idDest = "-1";
                //ver destinatario
                String origin = textsplitted[0];
                System.out.println("ORIGIN: " + origin);
                comandos(texto, textsplitted, idDest, origin);

                if (!texto.contains("/")) {
                    synchronized (mensajes) {
                        mensajes.addElement("PUBLIC FROM " + texto);
                        System.out.println(texto);
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable ex) {
            Logger.getLogger(HiloDeCliente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // Liberar recursos cuando el hilo termina
            // Cerrar el socket y los flujos
            try{
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (dataInput != null) {
                    dataInput.close();
                }
                if (dataOutput != null) {
                    dataOutput.close();
                }
            }
            catch(Exception e){}
            conectados.remove(this);
        }
    }

    public void reenviarAlmismosocket(String mensaje) {

        try {
            dataOutput.writeUTF(mensaje);
        } catch (IOException ex) {
            Logger.getLogger(HiloDeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        String texto = (String) mensajes.getElementAt(e.getIndex0());
        try {
            dataOutput.writeUTF(texto);
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void comandos(String texto, String[] textsplitted, String idDest, String origin) throws Throwable {

        //PRIVATE MSG
        //###############################################################
        if (texto.contains("/private")) {
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

            System.out.println(idDest);
            System.out.println(auxtext);

            for (HiloDeCliente h : conectados) {
                if (h.idserver.equalsIgnoreCase(idDest)) {
                    h.reenviarAlmismosocket("PRIVATE FROM " + origin + ": " + auxtext);
                }
            }
            for (HiloDeCliente h : conectados) {
                if (h.idserver.equalsIgnoreCase(origin)) {
                    h.reenviarAlmismosocket("PRIVATE TO " + idDest + ": " + auxtext);
                }
            }

            //CREATE GROUP
            //###############################################################
        } else if (texto.contains("/create group")) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/create") && textsplitted[i + 1].equals("group")) {
                    ArrayList<String> g = new ArrayList();
                    g.add(textsplitted[i + 2]);
                    g.add(origin);

                    groups.add(g);
                    this.reenviarAlmismosocket("GRUPO " + textsplitted[i + 2] + " CREADO\n");
                }

            }
            //INFO DEL USUARIO
            //###############################################################
        } else if (texto.contains("/whoami")) {
            for (HiloDeCliente h : conectados) {
                if (h.idserver.equalsIgnoreCase(origin)) {
                    h.reenviarAlmismosocket("CONNECTED AS " + origin + "\n");
                }
            }
         //UNIRSE A UN GRUPO
        //###############################################################
        } else if (texto.contains("/join group")) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/join") && textsplitted[i + 1].equals("group")) {

                    for (int j = 0; j < groups.size(); j++) {
                        ArrayList auxg = groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 2])) {
                            auxg.add(this.idserver);

                            
                            //reenviar a todos los del grupo
                            for (int k = 0; k < auxg.size(); k++) {
                                for (int l = 0; l < conectados.size(); l++) {
                                    if(conectados.get(l).idserver.equalsIgnoreCase(String.valueOf(auxg.get(k))))
                                    conectados.get(l).reenviarAlmismosocket(this.idserver +" UNIDO A GRUPO " + auxg.get(0));
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
        } else if (texto.contains("/group")) {

            String auxtext = "";
            for (int i = 0; i < textsplitted.length; i++) {

                if (textsplitted[i].equals("/group")) {

                    for (int j = i + 2; j < textsplitted.length; j++) {
                        auxtext += " " + textsplitted[j];
                    }

                    for (int j = 0; j < groups.size(); j++) {
                        ArrayList auxg = groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 1])) {

                            boolean entrar = false;

                            //validar que existe en ese grupo
                            for (int k = 1; k < auxg.size(); k++) {
                                if (origin.equals(String.valueOf(auxg.get(k)))) {
                                    entrar = true;
                                    break;
                                }
                            }

                            for (int k = 1; entrar && k < auxg.size(); k++) {
                                for (HiloDeCliente h : conectados) {
                                    if (h.idserver.equalsIgnoreCase(String.valueOf(auxg.get(k)))) {
                                        h.reenviarAlmismosocket("FROM GROUP " + textsplitted[i + 1] + " BY " + origin + ": " + auxtext);
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
        } else if (texto.contains("/help")) {

            this.reenviarAlmismosocket(""
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
        } else if (texto.contains("/leave group")) {

            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/leave") && textsplitted[i + 1].equals("group")) {

                    for (int j = 0; j < groups.size(); j++) {
                        ArrayList auxg = groups.get(j);
                        if (auxg.get(0).equals(textsplitted[i + 2])) {

                            for (int k = 1; k < auxg.size(); k++) {
                                if (this.idserver.equalsIgnoreCase(String.valueOf(auxg.get(k)))) {
                                    this.reenviarAlmismosocket("GRUPO " + textsplitted[i + 2] + " ABANDONADO\n");
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
        } else if (texto.contains("/allgroups")) {
            this.reenviarAlmismosocket("GRUPOS CREADOS:\n---------------------");
            for (int i = 0; i < groups.size(); i++) {
                ArrayList auxg = groups.get(i);
                this.reenviarAlmismosocket("    NOMBRE GRUPO: " + auxg.get(0) + "\n---------------------\nIntegrantes:");
                for (int j = 1; j < auxg.size(); j++) {
                    this.reenviarAlmismosocket("    ->ID: " + auxg.get(j) + "\n---------------------");
                }
            }
            //VER CONTECTADOS
            //###############################################################
        } else if (texto.contains("/status")) {
            this.reenviarAlmismosocket("ONLINE: \n---------------------");
            for (int i = 0; i < conectados.size(); i++) {
                this.reenviarAlmismosocket("ID: " + conectados.get(i).idserver + " IS CONNECTED\n---------------------");
            }
        } //EXIT
        //###############################################################
        else if (texto.contains("/exit")) {
            for (int i = 0; i < conectados.size(); i++) {
                conectados.get(i).reenviarAlmismosocket("ID: " + origin + " IS DISCONNECTED\n---------------------");
                if (conectados.get(i).idserver.equals(origin)) {
                    conectados.get(i).connected = false;
                }
            }

        }
    }

}
