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
        //ClienteChat clienteChat2 = new ClienteChat();
        /*
        ClienteChat clienteChat3 = new ClienteChat();
        ClienteChat clienteChat4 = new ClienteChat();
    */
    }


    public ClienteChat() throws Throwable {
        try {
            socket = new Socket("34.45.99.17", 80);
            ControlCliente control = new ControlCliente(socket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
