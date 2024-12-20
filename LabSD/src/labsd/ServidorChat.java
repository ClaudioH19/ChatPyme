package labsd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.DefaultListModel;

public class ServidorChat {

    private DefaultListModel mensajes = new DefaultListModel();




    int usuarios = 0;
    static boolean running = true;
    public static void main(String[] args) {
        new ServidorChat();

    }

    
    
    public ServidorChat() {
            int port=80;

        try {
            ServerSocket socketServidor = new ServerSocket(port);
            HiloDeCliente.conectados = new ArrayList();
            HiloDeCliente.groups = new ArrayList<ArrayList>();
            HiloDeCliente.historial= new ArrayList();
            System.out.println("SERVIDOR ACTIVO EN PUERTO: "+port);

            //clock
            Clock clock = new Clock();
            clock.start();

            Cache cache = new Cache();
            cache.start();

            while (running) {
                Socket cliente = socketServidor.accept();

                // Crea un nuevo objeto HiloDeCliente
                HiloDeCliente hc = new HiloDeCliente(mensajes, cliente);

                // Crea un nuevo hilo con el HiloDeCliente
                Thread hilo = new Thread(hc);

                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
                salida.writeUTF(hilo.getName()); // Enviar el ID único al cliente conectad
                // Inicia el hilo antes de asignar su ID
                hilo.start();




                //System.out.println("ENVIANDO: "+String.valueOf(hilo.getName()));
                //salida.writeUTF(hilo.getName()); // Enviar el ID único al cliente conectado

            }
            }catch (Exception e) {
            e.printStackTrace();
        }


    }

    


}
