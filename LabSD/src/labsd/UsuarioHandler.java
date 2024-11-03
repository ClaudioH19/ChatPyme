package labsd;

import com.mongodb.MongoException;

import java.util.ArrayList;

public class UsuarioHandler {
    private HiloDeCliente h;

    public UsuarioHandler(HiloDeCliente h) {
        this.h = h;
    }

    public void iniciarSesion(String texto, String[] textsplitted) {
        if (h.rol != null && !h.rol.isEmpty()) {
            h.reenviarAlmismosocket("#red#CIERRE SESIÓN PRIMERO */exit*");
            return;
        }
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/login")) {
                String correo = textsplitted[i + 1];
                String clave = textsplitted[i + 2];
                String[][] user = h.db.readuser(correo);
                if (user == null) {
                    h.reenviarAlmismosocket("#red#USUARIO NO ENCONTRADO");
                    return;
                }

                if (!user[3][1].equals(clave)) {
                    h.reenviarAlmismosocket("#red#CONTRASEÑA INCORRECTA");
                    return;
                }

                h.nombre = user[0][1];
                h.correo = user[1][1];
                h.rut = user[2][1];
                h.clave = user[3][1];
                h.rol = user[4][1];
                h.ingreso = Integer.parseInt(user[5][1]);
                h.reenviarAlmismosocket("*#blue#LOGUEADO COMO:* " + h.nombre + " <" + h.rol + ">");

                if (h.ingreso >= 1) {
                    h.ingreso += 1;
                }

                if (h.ingreso < 1) {
                    h.reenviarAlmismosocket("#red#Use */newpassword <clave>* PARA CAMBIAR SU CONTRASEÑA");
                }

                h.db.incrementingreso(h.correo);
                h.db.changestatus(h.correo, true);
                HiloDeCliente.conectados.add(h);

                for (HiloDeCliente cliente : HiloDeCliente.conectados) {
                    cliente.reenviarAlmismosocket("#blue#" + h.nombre + String.format(" *<%s>* ", h.rol) + " Connected");
                }

                if (!h.db.getchat(h.correo).isEmpty()) {
                    h.reenviarAlmismosocket("#orange#Hay Mensajes pendientes, escriba */read* para visualizar.");
                }
                h.inisesion = System.currentTimeMillis();
                MensajePrivadoHandler.users = h.db.getallusers();
                break;
            }
        }
    }

    public void cambiarContrasena(String texto, String[] textsplitted) {
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/newpassword") && h.ingreso <= 1) {
                h.clave = textsplitted[i + 1];
                h.ingreso += 1;
                h.db.updateuser(h.correo, "clave", h.clave);
                h.reenviarAlmismosocket("#BLUE# *CONTRASEÑA MODIFICADA");
                break;
            }
        }
    }

    public void leerMensajesPendientes() {
        h.reenviarAlmismosocket("#orange#*leyendo...");
        ArrayList<String> mensajes = h.db.getchat(h.correo);
        for (String mensaje : mensajes) {
            h.reenviarAlmismosocket(mensaje);
        }
        h.db.dropmensajes(h.correo);
    }

    public void registrarUsuario(String texto, String[] textsplitted) {
        if (!h.rol.equals("administrador")) {
            h.reenviarAlmismosocket("*#red#SOLO ADMINISTRADORES PUEDEN REGISTRAR");
            return;
        }
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/register")) {
                try {
                    String nombre = textsplitted[i + 1];
                    String correo = textsplitted[i + 2];
                    String rut = textsplitted[i + 3];
                    String clave = textsplitted[i + 4];
                    String rol = textsplitted[i + 5].toLowerCase();

                    if (rol.equals("medico")) {
                        rol = "médico";
                    }
                    if (!rol.matches("^(médico|administrativo)$")) {
                        h.reenviarAlmismosocket("#red#ROL NO VÁLIDO. SOLO SE PERMITEN LOS ROLES 'médico' O 'administrativo'.");
                        return;
                    }

                    h.db.createuser(nombre, correo, rut, clave, rol);
                    h.reenviarAlmismosocket("#blue#USUARIO " + nombre + " CREADO");
                } catch (MongoException e) {
                    h.reenviarAlmismosocket(String.valueOf(e));
                }
                break;
            }
        }
    }
}
