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

    public static void main(String[] args) {
        new ServidorChat();
    }

    
    
    public ServidorChat() {
        try {
            ServerSocket socketServidor = new ServerSocket(5000);
            HiloDeCliente.conectados = new ArrayList();
            HiloDeCliente.groups = new ArrayList<ArrayList>();
            HiloDeCliente.historial= new ArrayList();

            while (true) {
                Socket cliente = socketServidor.accept();

                // Crea un nuevo objeto HiloDeCliente
                HiloDeCliente hc = new HiloDeCliente(mensajes, cliente);

                // Crea un nuevo hilo con el HiloDeCliente
                Thread hilo = new Thread(hc);

                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
                salida.writeUTF(hilo.getName()); // Enviar el ID único al cliente conectad
                // Inicia el hilo antes de asignar su ID
                hilo.start();

                //clock
                Clock clock = new Clock();
                clock.start();


                System.out.println("ENVIANDO: "+String.valueOf(hilo.getName()));
                //salida.writeUTF(hilo.getName()); // Enviar el ID único al cliente conectado


        }

            }catch (Exception e) {
            e.printStackTrace();
        }

        }

    


}
