package labsd;

import com.mongodb.MongoException;

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

    protected String nombre;
    protected String correo;
    protected String rut;
    protected String clave;
    protected String rol;

    public String idserver;
    public boolean connected;
    public Basededatos db;
    public Comandos cmd;

    public HiloDeCliente(DefaultListModel mensajes, Socket socket) {
        this.idserver = "";
        this.nombre="";
        this.correo="";
        this.rut="";
        this.clave="";
        this.rol="";
        this.mensajes = mensajes;
        this.socket = socket;
        this.connected = true;
        this.db = new Basededatos();
        this.cmd = new Comandos(this);

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
        this.reenviarAlmismosocket("USE /login <correo> <clave> PARA INGRESAR.");
        this.idserver = Thread.currentThread().getName();


        try {
            while (connected) {
                String texto = dataInput.readUTF();
                String[] textsplitted = texto.split(" ");

                String textosinid="";
                for (int i = 1; i < textsplitted.length; i++) {
                    textosinid+=textsplitted[i];
                    if(i<textsplitted.length-1){
                        textosinid+=" ";
                    }
                }

                //EJECUTAR COMANDOS------------------------------------------------

                String idDest = "-1";
                //ver destinatario
                String origin = textsplitted[0];
                System.out.println("ORIGIN: " + origin);

                cmd.comandos(texto, textsplitted, idDest, origin);


                //SALA PUBLICA DESCARTAR POR ROLES---------------------------------
                if (!texto.contains("/") && cmd.validationrol()) {
                    synchronized (mensajes) {
                        for(HiloDeCliente h : conectados){
                            if(h.rol.equals(this.rol)){
                                h.reenviarAlmismosocket(String.format("[PUBLIC FOR <%s> %s]: %s",this.rol,this.nombre,textosinid));
                            }

                        }
                        //mensajes.addElement("[PUBLIC "+this.nombre+ "]<"+this.rol+">: "+ textosinid);
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

}
