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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;


public class ControlCliente implements ActionListener, Runnable {

    private DataInputStream dataInput;
    private DataOutputStream dataOutput;
    private PanelCliente panel;
    private Thread hilo;
    private String IDserver;
    public boolean connected;
    private Socket socket;
    private boolean error=false;
    File file;
    int intento=0;

    JFrame v;
    Basededatos db = new Basededatos();

    public ArrayList<String> msg_noenviados = new ArrayList<String>();

    public ControlCliente(Socket socket) throws IOException {
        this.socket = socket;
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

        ///abrir un arhcivo para copias locales
        String filePath = "savelocal.txt";
        file = new File(filePath);
        if (!file.exists()) {
            // Si no existe, intenta crearlo
            if (file.createNewFile()) {
                System.out.println("Ruta local creada: " + filePath);
            } else {
                throw new IOException("No se pudo crear la ruta de copias locales.");
            }
        }
    }


    public void saveText(File file, String msg) throws IOException {
        System.out.println("A GUARDAR: "+msg);
        panel.addTexto(msg+"\n",false,false,false,Color.BLACK);
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(msg + System.lineSeparator());
            System.out.println("Mensaje guardado");
        }
    }

    public void subirLocal() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            System.out.println("Subiendo pendientes");
            while ((line = reader.readLine()) != null) {
                String[] arr_aux = line.split(" ");
                //solo si lo guardado pertenece a este usuario se enviará
                if(arr_aux.length>0 && arr_aux[0].equals(IDserver))
                    dataOutput.writeUTF(line);
            }
            //borrar contenido
            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write("");
                System.out.println("Local subido y borrado");
            }
        }
    }


    public void mostrarMensajes(String mensaje){
        String[] textsplitted=mensaje.split(" ");
        for (int i=0; i<textsplitted.length; i++) {
            if (textsplitted[i].equals("/msgs")) {
                ArrayList<String> mensajes = db.getmensajes(textsplitted[i + 1]);
                for (String s : mensajes) {
                    panel.addTexto(s+"\n",false,false,false,Color.BLACK);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evento) {
        String mensaje=IDserver + " " + panel.getTexto()+" ";
        try {//los espacios ayudan a parsear el texto por lado del servidor, es util para separar datos del mensaje
            dataOutput.writeUTF(mensaje);
        } catch (Exception excepcion) {
            try {
                mostrarMensajes(mensaje);
                System.out.println("MENSAJE: "+mensaje);
                saveText(file,mensaje);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            excepcion.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            while (connected) {
                String texto="";
                try {
                    // Lee el mensaje
                    texto = dataInput.readUTF();
                } catch (IOException excepcion) {
                    connected = false;  // Marca como desconectado para intentar reconectar
                    error=true;
                    panel.addTexto("Se ha perdido la conexión, cierre la aplicación o espere reconexión. Los mensajes quedarán en forma local\n",false,false,false,Color.RED);
                    db.changestatus(this.IDserver, false);
                }

                if(texto.contains("/loggedsucceed")) { //esta es la bandera enviada que reporta un logueo

                    //haremos un enroque entre el idserver inicial con el correo ahora que existe
                    String[] array =texto.split(" ");
                    this.IDserver = array[array.length-1]; //aquí está el correo
                    try {
                        if(texto.contains(this.IDserver))
                            subirLocal();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    parsearTexto(texto);

                }

                panel.addTexto("\n",false,false,false, Color.BLACK);
            }

            //zona de desconexion--------------------------------------------------------------------------
            if(error){


                while (error) {
                    if(intento>3){
                        error=false;
                        break;
                    }
                    try {
                        Thread.sleep(3000);  // esperar antes de intentar reconectar

                        panel.addTexto("Intentando reconectar... "+ intento++ +"/3 \n", false, false, false, Color.RED);

                        // intenta reconectar el socket
                        this.socket = new Socket("34.31.215.146", 80);
                        dataInput = new DataInputStream(socket.getInputStream());
                        dataOutput = new DataOutputStream(socket.getOutputStream());
                        connected = true;  // Marca como reconectado
                        error = false;  // Salir del ciclo de reconexión

                        // Reiniciar el hilo
                        hilo = new Thread(this);
                        hilo.start();

                        SwingUtilities.invokeLater(() -> panel.addTexto("Reconexión exitosa\n", true, false, false, Color.GREEN));
                    } catch (IOException e) {
                        // si falla mostrar mensaje y continuar el ciclo
                        SwingUtilities.invokeLater(() -> panel.addTexto("Reconexión fallida. Reintentando...\n", false, false, false, Color.RED));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Manejar la interrupción del hilo
                    }
                }


            }


        } catch (Exception e) {
            e.printStackTrace();
            Thread.interrupted();
        }
    }




    public void parsearTexto(String texto) throws IOException {
        boolean underline=false;
        boolean bold=false;
        boolean italic=false;
        Color color= Color.BLACK;
        //parsear texto
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
    }





    private void creaYVisualizaVentana() {
        v = new JFrame();
        panel = new PanelCliente(v.getContentPane());
        v.pack();
        v.setTitle("ChatPyme");
        v.setVisible(true);
        v.setSize(1200, 720);
        v.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        v.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    dataOutput.writeUTF("/exit");
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
