package labsd;

import javax.swing.*;
import java.awt.*;
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
import java.lang.reflect.Field;

public class ControlCliente implements ActionListener, Runnable {

    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private Thread hilo;
    private String IDserver;
    public boolean connected;


    JFrame v;

    public ControlCliente(Socket socket) {
        this.connected = true;
        //this.panel = panel;
        try {
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

                boolean underline=false;
                boolean bold=false;
                boolean italic=false;
                Color color= Color.BLACK;
                //parsear texto
                System.out.println(texto);
                for (int i = 0; i < texto.length(); i++) {
                    String hex="";
                    if(texto.charAt(i)=='_'){
                        underline=!underline;
                    }


                    if(texto.charAt(i)=='*'){
                        bold=!bold;
                    }


                    if(texto.charAt(i)=='~'){
                        italic=!italic;
                    }

                    if (texto.charAt(i) == '#') {
                        String aux = "";
                        for (i = i + 1; i < texto.length() && texto.charAt(i) != '#'; i++) {
                            aux += texto.charAt(i);
                        }
                        if (i < texto.length() - 1)
                            i += 1;

                        try {
                            Field field = Color.class.getField(aux.toUpperCase());
                            color = (Color) field.get(null);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            // Si el campo no existe, asignamos un color predeterminado (ej. negro)
                            System.err.println("Color no reconocido: " + aux + ". Se usará el color por defecto (negro).");
                            color = Color.BLACK;
                        }
                    }



                    // Convertir el color a su valor hexadecimal
                    hex = String.format("#%06X", color.getRGB() & 0xFFFFFF);
                    color = Color.decode(hex);
                    if( (texto.charAt(i)!='*') && (texto.charAt(i)!='~') && (texto.charAt(i)!='_'))
                        panel.addTexto(texto.charAt(i)+"",bold,italic,underline,color);


                }

                panel.addTexto("\n",false,false,false, Color.BLACK);
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
