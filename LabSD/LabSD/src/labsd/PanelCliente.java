package labsd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class PanelCliente {

    private JScrollPane scroll;
    private JTextArea textArea;
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
    private JButton btnHelp;

    public PanelCliente(Container contenedor) {
        // Colores oscuros estilo Discord
        Color backgroundDark = new Color(54, 57, 63);
        Color buttonColor = new Color(88, 101, 242);
        Color textColor = new Color(220, 221, 222);
        Color buttonHoverColor = new Color(114, 137, 218);

        // Establecer el layout y estilo de la interfaz
        contenedor.setLayout(new BorderLayout());
        contenedor.setBackground(backgroundDark);

        // Área de texto para mensajes (más grande)
        textArea = new JTextArea();
        textArea.setBackground(backgroundDark);
        textArea.setForeground(textColor);
        textArea.setFont(new Font("Arial", Font.PLAIN, 18)); // Letra más grande
        textArea.setEditable(false);  // Desactivar edición en el área de texto
        textArea.setLineWrap(true);   // Ajustar el texto en varias líneas
        scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(1200, 600));  // Aumentar el tamaño del panel de mensajes
        scroll.getViewport().setBackground(backgroundDark);

        // Parte de entrada de mensaje (más grande)
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(backgroundDark);

        textField = new JTextField(100);  // Aumentar el tamaño del campo de texto
        textField.setBackground(new Color(64, 68, 75));
        textField.setForeground(textColor);
        textField.setFont(new Font("Arial", Font.PLAIN, 20));  // Letra más grande

        boton = createButton("Enviar", buttonColor, textColor, buttonHoverColor); // Botón más pequeño
        limpiarBoton = createButton("Limpiar", buttonColor, textColor, buttonHoverColor); // Botón de limpiar

        // Panel de comandos con botones reorganizados
        JPanel panelComandos = new JPanel(new GridLayout(2, 5, 10, 10));  // Ajuste para más botones con más espacio
        panelComandos.setBackground(backgroundDark);

        // Inicialización de botones organizados por grupos y funciones
        // Botones relacionados con grupos
        btnCreateGroup = createButton("Crear Grupo", buttonColor, textColor, buttonHoverColor);
        btnJoinGroup = createButton("Unirse Grupo", buttonColor, textColor, buttonHoverColor);
        btnAllGroups = createButton("Ver Grupos", buttonColor, textColor, buttonHoverColor);
        btnGroupMessage = createButton("Msg Grupo", buttonColor, textColor, buttonHoverColor);
        btnLeaveGroup = createButton("Salir Grupo", buttonColor, textColor, buttonHoverColor);

        // Botones de estado/información
        btnPrivate = createButton("Privado", buttonColor, textColor, buttonHoverColor);
        btnWhoami = createButton("¿Quién soy?", buttonColor, textColor, buttonHoverColor);
        btnStatus = createButton("Estado Servidor", buttonColor, textColor, buttonHoverColor);
        btnHelp = createButton("Ayuda", buttonColor, textColor, buttonHoverColor);

        // Agregar botones al panel de comandos en el orden solicitado
        panelComandos.add(btnCreateGroup);    // Botones relacionados con grupos
        panelComandos.add(btnJoinGroup);
        panelComandos.add(btnAllGroups);
        panelComandos.add(btnGroupMessage);
        panelComandos.add(btnLeaveGroup);

        panelComandos.add(btnPrivate);        // Botones de estado/información
        panelComandos.add(btnWhoami);
        panelComandos.add(btnStatus);
        panelComandos.add(btnHelp);
        panelComandos.add(limpiarBoton);      // Botón para limpiar la pantalla

        // Agregar el panel de comandos y el campo de texto
        panel.add(textField, BorderLayout.CENTER);  // Campo de texto más grande
        panel.add(boton, BorderLayout.EAST);  // Botón más pequeño a la derecha

        // Contenedor principal
        JPanel contenedorPrincipal = new JPanel(new BorderLayout());
        contenedorPrincipal.add(panel, BorderLayout.NORTH);
        contenedorPrincipal.add(panelComandos, BorderLayout.CENTER);

        contenedor.add(scroll, BorderLayout.CENTER);  // Más espacio para el área de texto
        contenedor.add(contenedorPrincipal, BorderLayout.SOUTH);

        // Asignar acciones a los botones
        btnPrivate.addActionListener(e -> addCommandToTextField("/private "));
        btnWhoami.addActionListener(e -> simulateEnter("/whoami"));
        btnCreateGroup.addActionListener(e -> addCommandToTextField("/create group "));
        btnJoinGroup.addActionListener(e -> addCommandToTextField("/join group "));
        btnAllGroups.addActionListener(e -> simulateEnter("/allgroups"));
        btnGroupMessage.addActionListener(e -> addCommandToTextField("/group "));
        btnLeaveGroup.addActionListener(e -> addCommandToTextField("/leave group "));
        btnStatus.addActionListener(e -> simulateEnter("/status"));
        btnHelp.addActionListener(e -> simulateEnter("/help"));

        // Asignar acción para el botón "Limpiar"
        limpiarBoton.addActionListener(e -> textArea.setText(""));
    }

    // Método auxiliar para crear botones con estilo personalizado (sin redondeo)
    private JButton createButton(String text, Color bgColor, Color fgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setForeground(fgColor);
        button.setFont(new Font("Arial", Font.BOLD, 14));  // Botón con fuente más pequeña
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 40));  // Botones más pequeños
        button.setBackground(bgColor);

        // Efecto hover (cambia el color cuando el mouse pasa sobre el botón)
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

    public void addTexto(String texto) {
        textArea.append(texto + "\n");
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
        boton.doClick();  // Simula el Enter
    }
}
