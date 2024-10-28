package labsd;


import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.WindowConstants;


public class ClienteChat {


    private Socket socket;
    private PanelCliente panel;

    public static void main(String[] args) throws Throwable {
        
        
        System.out.println("Iniciando ClienteChat...");
        ClienteChat clienteChat = new ClienteChat();
    }

    public ClienteChat() throws Throwable {
        try {
            socket = new Socket("localhost", 5000);
            ControlCliente control = new ControlCliente(socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
