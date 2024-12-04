package labsd;

import com.mongodb.MongoException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class HiloDeCliente implements Runnable, ListDataListener {

    static public ArrayList<HiloDeCliente> conectados;
    static public ArrayList<ArrayList> groups;
    static public ArrayList<String> historial;
    private DefaultListModel mensajes;
    private Socket socket;
    private DataInputStream dataInput;
    private DataOutputStream dataOutput;

    protected String nombre;
    protected String correo;
    protected String rut;
    protected String clave;
    protected String rol;
    protected int ingreso;

    public String idserver;
    public boolean connected;
    public Basededatos db;
    public Comandos cmd;

    // Estadísticas
    public long inisesion;
    public int cantmsg;
    private Map<String, Integer> interaccionesConUsuarios; // pa contar interacciones con otros

    public HiloDeCliente(DefaultListModel mensajes, Socket socket) {
        this.idserver = "";
        this.nombre = "";
        this.correo = "";
        this.rut = "";
        this.clave = "";
        this.rol = "";
        this.ingreso = -1;
        this.mensajes = mensajes;
        this.socket = socket;
        this.connected = true;
        this.db = new Basededatos();
        this.cmd = new Comandos(this);
        this.inisesion = 0;
        this.cantmsg = 0;
        this.interaccionesConUsuarios = new HashMap<>(); // mapa de interacciones

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

        this.reenviarAlmismosocket("#magenta#Para ingresar: */login <correo> <clave>* PARA INGRESAR.");
        this.idserver = Thread.currentThread().getName();

        try {
            while (connected) {
                String texto = dataInput.readUTF();
                String[] textsplitted = texto.split(" ");

                String textosinid = "";
                for (int i = 1; i < textsplitted.length; i++) {
                    textosinid += textsplitted[i];
                    if (i < textsplitted.length - 1) {
                        textosinid += " ";
                    }
                }

                // Ejecutar comandos
                String idDest = "-1";
                String origin = textsplitted[0];
                System.out.println("ORIGIN: " + origin);

                cmd.comandos(texto, textsplitted, idDest, origin);

                // Sala pública según roles
                if (!texto.contains("/") && cmd.validationrol()) {
                    synchronized (mensajes) {
                        for (HiloDeCliente h : conectados) {
                            if (h.rol.equals(this.rol)) {
                                h.reenviarAlmismosocket(String.format("*#magenta#[PUBLIC FOR <%s> %s]:*#black# %s", this.rol, this.nombre, textosinid));
                            }
                        }
                        System.out.println(texto);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable ex) {
            Logger.getLogger(HiloDeCliente.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (dataInput != null) {
                    dataInput.close();
                }
                if (dataOutput != null) {
                    dataOutput.close();
                }
            } catch (Exception e) {}
            conectados.remove(this);
        }
    }

    // interacción con otro usuario
    public void registrarInteraccion(String idUsuario) {
        interaccionesConUsuarios.put(idUsuario, interaccionesConUsuarios.getOrDefault(idUsuario, 0) + 1);
    }
    // Enviar mensaje a otro cliente y registrar la interacción
    public void enviarMensajeACliente(HiloDeCliente destinatario, String mensaje) {
        try {
            destinatario.dataOutput.writeUTF(mensaje);
            destinatario.cantmsg++;
            destinatario.historial.add(mensaje);

            // Registrar la interacción solo en el remitente
            if (!this.idserver.equals(destinatario.idserver)) { // Asegúrate de no contar al propio usuario
                this.registrarInteraccion(destinatario.idserver);
            }
        } catch (IOException ex) {
            Logger.getLogger(HiloDeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // Envía mensaje y registra interacción
    public void reenviarAlmismosocket(String mensaje) {
        try {
            dataOutput.writeUTF(mensaje);
            this.cantmsg++;
            this.historial.add(mensaje);
            registrarInteraccion(this.idserver); // Registra la interacción
        } catch (IOException ex) {
            Logger.getLogger(HiloDeCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Obtiene el conteo de mensajes con un usuario específico
    public int obtenerInteraccionConUsuario(String idUsuario) {
        return interaccionesConUsuarios.getOrDefault(idUsuario, 0);
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        String texto = (String) mensajes.getElementAt(e.getIndex0());
        try {
            dataOutput.writeUTF(texto);
            this.cantmsg++;
            this.historial.add(texto);
            registrarInteraccion(this.idserver); // Registra interacción
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {}

    @Override
    public void contentsChanged(ListDataEvent e) {}


    public static synchronized void removeUserDisconected(String correo){
        for (HiloDeCliente h: conectados){
            if(correo.equals(h.correo)){
                conectados.remove(h);
                return;
            }
        }
    }

}
