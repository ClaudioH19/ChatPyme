package labsd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class PanelCliente {

    Basededatos db = new Basededatos();

    private JScrollPane scroll;
    private JTextPane textArea;
    private JTextField textField;
    private JButton boton;
    private JButton limpiarBoton;

    // Comandos adicionales
    private JButton btnPrivate;
    private JButton btnWhoami;
    private JButton btnCreateGroup;
    private JButton btnJoinGroup;
    private JButton btnAllGroups;
    private JButton btnGroupMessage;
    private JButton btnLeaveGroup;
    private JButton btnStatus;
    private JButton btnlogin;

    private JButton btncolor; // Declaración del botón de color
    private Color selectedColor = Color.BLACK;


    public PanelCliente(Container contenedor) {
        // Colores estilo centro médico
        Color backgroundWhite = Color.WHITE;
        Color buttonBlue = new Color(66, 133, 244);       // Azul
        Color buttonGreen = new Color(102, 187, 106);     // Verde claro
        Color buttonRed = new Color(244, 67, 54);         // Rojo
        Color textColor = Color.WHITE;
        Color hoverBlue = new Color(50, 105, 168);
        Color hoverGreen = new Color(76, 139, 82);        // Verde oscuro para efecto hover
        Color hoverRed = new Color(194, 54, 44);


        // Establecer el layout y estilo de la interfaz
        contenedor.setLayout(new BorderLayout());
        contenedor.setBackground(backgroundWhite);

        // Área de texto para mensajes
        textArea = new JTextPane();
        textArea.setBackground(backgroundWhite);
        textArea.setForeground(Color.BLACK); // Texto negro en fondo blanco
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));
        textArea.setEditable(false);
        //textArea.setLineWrap(true);
        scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(1200, 600));
        scroll.getViewport().setBackground(backgroundWhite);

        // Parte de entrada de mensaje
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundWhite);

        textField = new JTextField(100);
        textField.setBackground(new Color(230, 230, 230)); // Fondo claro para campo de texto
        textField.setForeground(Color.BLACK);
        textField.setFont(new Font("Arial", Font.PLAIN, 20));

        boton = createButton("Enviar", buttonGreen, textColor, hoverGreen);


        // Panel de comandos con botones reorganizados
        JPanel panelComandos = new JPanel(new GridLayout(2, 5, 10, 10));
        panelComandos.setBackground(backgroundWhite);

        // Inicialización de botones organizados por grupos y funciones
        btnCreateGroup = createButton("Crear Grupo", buttonBlue, textColor, hoverBlue);
        btnJoinGroup = createButton("Unirse Grupo", buttonBlue, textColor, hoverBlue);
        btnAllGroups = createButton("Ver Grupos", buttonBlue, textColor, hoverBlue);
        btnGroupMessage = createButton("Msg Grupo", buttonBlue, textColor, hoverBlue);
        btnLeaveGroup = createButton("Salir Grupo", buttonRed, textColor, hoverRed);

        btnPrivate = createButton("Privado", buttonGreen, textColor, hoverGreen);
        btnWhoami = createButton("Info. sesión", buttonGreen, textColor, hoverGreen);
        btnStatus = createButton("Conectados", buttonGreen, textColor, hoverGreen);
        btnlogin = createButton("Log-in", buttonGreen, textColor, hoverGreen);
        limpiarBoton = createButton("Limpiar", buttonRed, textColor, hoverRed);

        // Crear el botón de selección de color
        btncolor = createButton("Color", buttonBlue, textColor, hoverBlue);
        btncolor.setPreferredSize(new Dimension(80, 40));
        btncolor.addActionListener(e -> {
            Color color = JColorChooser.showDialog(null, "Seleccione un color", selectedColor);
            if (color != null) {
                selectedColor = color; // Actualiza el color seleccionado
            }
        });


        // Agregar botones al panel de comandos en el orden solicitado
        panelComandos.add(btnCreateGroup);
        panelComandos.add(btnJoinGroup);
        panelComandos.add(btnAllGroups);
        panelComandos.add(btnGroupMessage);
        panelComandos.add(btnLeaveGroup);

        panelComandos.add(btnPrivate);
        panelComandos.add(btnWhoami);
        panelComandos.add(btnStatus);
        panelComandos.add(btnlogin);
        panelComandos.add(limpiarBoton);

        // Agregar el panel de comandos y el campo de texto
       // panel.add(btncolor, BorderLayout.WEST); // Botón de color al lado izquierdo
        panel.add(textField, BorderLayout.CENTER);
        panel.add(boton, BorderLayout.EAST);

        // Contenedor principal
        JPanel contenedorPrincipal = new JPanel(new BorderLayout());
        contenedorPrincipal.add(panel, BorderLayout.NORTH);
        contenedorPrincipal.add(panelComandos, BorderLayout.CENTER);

        contenedor.add(scroll, BorderLayout.CENTER);
        contenedor.add(contenedorPrincipal, BorderLayout.SOUTH);

        // Asignar acciones a los botones
        btnPrivate.addActionListener(e -> addCommandToTextField("/private"));
        btnWhoami.addActionListener(e -> simulateEnter("/whoami"));
        btnCreateGroup.addActionListener(e -> addCommandToTextField("/create group "));
        btnJoinGroup.addActionListener(e -> addCommandToTextField("/join group "));
        btnAllGroups.addActionListener(e -> simulateEnter("/allgroups"));
        btnGroupMessage.addActionListener(e -> addCommandToTextField("/group "));
        btnLeaveGroup.addActionListener(e -> addCommandToTextField("/leave group "));
        btnStatus.addActionListener(e -> simulateEnter("/status"));
        btnlogin.addActionListener(e -> addCommandToTextField("/login"));

        // Asignar acción para el botón "Limpiar"
        limpiarBoton.addActionListener(e -> textArea.setText(""));
    }

        private JButton createButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
            JButton button = new JButton(text);
            button.setForeground(fgColor);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(120, 40));
            button.setBackground(bgColor);

            // Efecto hover (cambia el color a azul claro cuando el mouse pasa sobre el botón)
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(hoverColor);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(bgColor);
                }
            });

        return button;
    }

    public void addActionListener(ActionListener accion) {
        textField.addActionListener(accion);
        boton.addActionListener(accion);
    }

    public void addTexto(String texto, boolean bold, boolean italic, boolean underline,Color color) {
        StyledDocument doc = textArea.getStyledDocument();
        Style style = textArea.addStyle("Estilo", null);

        // Aplicar estilos según los parámetros
        StyleConstants.setBold(style, bold);
        StyleConstants.setItalic(style, italic);
        StyleConstants.setUnderline(style, underline);
        StyleConstants.setForeground(style, color);

        try {
            // Inserta el texto al final del documentor
            doc.insertString(doc.getLength(), texto, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public String getTexto() {
        String texto = textField.getText();
        textField.setText("");
        return texto;
    }

    // Método auxiliar para añadir comandos al campo de texto
    private void addCommandToTextField(String command) {
        textField.setText(command);
        textField.requestFocus();
    }

    // Método que simula un Enter después de agregar el texto al campo
    private void simulateEnter(String command) {
        textField.setText(command);
        textField.requestFocus();
        boton.doClick();
    }
}