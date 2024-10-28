package labsd;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControlCliente implements ActionListener, Runnable {

    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private Thread hilo;
    private String IDserver;
    public boolean connected;


    private String nombre;
    private String correo;
    private String rut;
    private String clave;
    private String rol;


    JFrame v;

    public ControlCliente(Socket socket) {
        this.connected = true;
        //this.panel = panel;
        try {
            this.nombre=this.rol=this.correo=this.rut=this.clave="null";
            dataInput = new DataInputStream(socket.getInputStream());
            dataOutput = new DataOutputStream(socket.getOutputStream());
            IDserver = dataInput.readUTF();
            System.out.println(IDserver);
            creaYVisualizaVentana();
            //panel.addTexto("CONECTADO COMO: "+IDserver+"\n");

            panel.addActionListener(this);
            hilo = new Thread(this);
            hilo.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evento) {
        try {//los espacios ayudan a parsear el texto por lado del servidor, es util para separar datos del mensaje
            dataOutput.writeUTF(IDserver + " " + panel.getTexto()+" ");
        } catch (Exception excepcion) {
            excepcion.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (connected) {
                String texto = dataInput.readUTF();
                panel.addTexto(texto);
                panel.addTexto("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void creaYVisualizaVentana() {
        v = new JFrame();
        panel = new PanelCliente(v.getContentPane());
        v.pack();
        v.setTitle("CONECTADO COMO: " + IDserver + "\n");
        v.setVisible(true);
        v.setSize(1200, 720);
        v.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        v.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataOutput.writeUTF(IDserver + " /exit");
                } catch (IOException ex) {
                    Logger.getLogger(ControlCliente.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    connected = false;

                    v.dispose();
                }
            }
        });

    }

}
