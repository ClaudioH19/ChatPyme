package labsd;

import java.util.ArrayList;

public class SistemaHandler {
    private HiloDeCliente h;

    public SistemaHandler(HiloDeCliente h) {
        this.h = h;
    }

    public void mostrarAyuda() {
        h.reenviarAlmismosocket(
                "\nComandos disponibles:\n" +
                        "/whoami ----------> Muestra la información de sesión\n" +
                        "/private  --------> Envía un mensaje privado al ID \n" +
                        "/allgroups --------> Muestra todos los grupos creados\n" +
                        "/group  ----------> Envía un mensaje en el grupo \n" +
                        "/create group  --> Crea un grupo con el nombre \n" +
                        "/join group  -----> Se une al grupo de nombre \n" +
                        "/leave group  ---> Se sale del grupo de nombre \n" +
                        "/status ------------> Muestra el estado del servidor\n" +
                        "/login -------------> Inicia sesión con correo y clave\n" +
                        "/newpassword <clave> --> Cambia la contraseña\n" +
                        "/read --------------> Lee mensajes pendientes\n" +
                        "/register ----------> Registra un nuevo usuario (solo administradores)\n" +
                        "/admin -------------> Enviar mensaje a roles administrativos\n" +
                        "/aux ---------------> Enviar mensaje a auxiliares\n" +
                        "/urgency -----------> Enviar mensaje de emergencia (solo administradores)\n" +
                        "/stats -------------> Muestra estadísticas de usuarios conectados (solo administradores)\n" +
                        "/exit --------------> Desconecta al cliente\n"
        );
    }

    public void mostrarEstado() {
        h.reenviarAlmismosocket("#green#*ONLINE:* \n---------------------");
        for (HiloDeCliente cliente : HiloDeCliente.conectados) {
            String nombre = "";
            String rol = "";
            String correo = "";

            for (HiloDeCliente conectado : HiloDeCliente.conectados) {
                if (conectado.idserver.equalsIgnoreCase(cliente.idserver)) {
                    nombre = conectado.nombre;
                    rol = conectado.rol;
                    correo = conectado.correo;
                }
            }

            h.reenviarAlmismosocket(String.format("#blue#User: %s %s <%s> IS #green#CONNECTED\n---------------------", nombre, correo, rol));
        }
    }

    public void enviarMensajeAAdministradores(String texto, String[] textsplitted) {
        if (!h.rol.equals("medico")) return;

        String auxtext = "";
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/admin")) {
                for (int j = i + 1; j < textsplitted.length; j++) {
                    auxtext += " " + textsplitted[j];
                }
            }
        }

        ArrayList<String[][]> users = h.db.getallusers();
        for (String[][] s : users) {
            if (s[4][1].toLowerCase().matches("admision|examenes|pabellon")) {
                if (!h.db.readstatus(s[1][1])) {
                    h.db.addmensajes(s[1][1], "#blue#[PUBLIC FOR *<Administradores>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
                } else {
                    for (HiloDeCliente conectado : HiloDeCliente.conectados) {
                        if (conectado.correo.equals(s[1][1])) {
                            conectado.reenviarAlmismosocket("#blue#[PUBLIC FOR *<Administradores>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
                        }
                    }
                }
            }
        }
        h.reenviarAlmismosocket("#blue#[PUBLIC FOR *<Administradores>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
    }

    public void enviarMensajeAAuxiliares(String texto, String[] textsplitted) {
        String auxtext = "";
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/aux")) {
                for (int j = i + 1; j < textsplitted.length; j++) {
                    auxtext += " " + textsplitted[j];
                }
            }
        }

        ArrayList<String[][]> users = h.db.getallusers();
        if (h.rol.equals("auxiliar")) {
            for (String[][] s : users) {
                if (!h.db.readstatus(s[1][1])) {
                    h.db.addmensajes(s[1][1], "#blue#[PUBLIC FOR *<ALL>* BY Auxiliar:" + h.correo + "]: " + "#black#" + auxtext);
                } else {
                    for (HiloDeCliente conectado : HiloDeCliente.conectados) {
                        if (conectado.correo.equals(s[1][1])) {
                            conectado.reenviarAlmismosocket("#blue#[PUBLIC FOR *<ALL>* BY Auxiliar:" + h.correo + "]: " + "#black#" + auxtext);
                        }
                    }
                }
            }
        } else {
            for (String[][] s : users) {
                if (s[4][1].toLowerCase().equals("auxiliar")) {
                    if (!h.db.readstatus(s[1][1])) {
                        h.db.addmensajes(s[1][1], "#blue#[PUBLIC FOR *<Auxiliares>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
                    } else {
                        for (HiloDeCliente conectado : HiloDeCliente.conectados) {
                            if (conectado.correo.equals(s[1][1])) {
                                conectado.reenviarAlmismosocket("#blue#[PUBLIC FOR *<Auxiliares>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
                            }
                        }
                    }
                }
            }
            h.reenviarAlmismosocket("#blue#[PUBLIC FOR *<Auxiliares>* BY " + h.rol + " " + h.correo + "]: " + "#black#" + auxtext);
        }
    }

    public void enviarMensajeDeEmergencia(String texto, String[] textsplitted) {
        if (!h.rol.equals("administrador")) {
            h.reenviarAlmismosocket("#red#*No permitido*");
            return;
        }

        String auxtext = "";
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/urgency")) {
                for (int j = i + 1; j < textsplitted.length; j++) {
                    auxtext += " " + textsplitted[j];
                }
            }
        }

        ArrayList<String[][]> users = h.db.getallusers();
        for (String[][] s : users) {
            if (!h.db.readstatus(s[1][1])) {
                h.db.addmensajes(s[1][1], "*#red#[URGENCY]: " + "#orange#" + auxtext);
            } else {
                for (HiloDeCliente conectado : HiloDeCliente.conectados) {
                    if (conectado.correo.equals(s[1][1])) {
                        conectado.reenviarAlmismosocket("*#red#[URGENCY]: " + "#orange#" + auxtext);
                    }
                }
            }
        }
    }
    public void desconectarCliente(String origin) {
        h.db.changestatus(h.correo, false);
        for (int i = 0; i < HiloDeCliente.conectados.size(); i++) {
            HiloDeCliente cliente = HiloDeCliente.conectados.get(i);
            cliente.reenviarAlmismosocket("\n---------------------\n#red#" + h.nombre + " is disconnected\n---------------------");
            if (cliente.idserver.equals(origin)) {
                cliente.connected = false;
                HiloDeCliente.conectados.remove(i);
                break;
            }
        }
        h.reenviarAlmismosocket("#red#Has sido desconectado.");
    }

    public void mostrarEstadisticas() {
        if (!h.rol.equals("administrador")) {
            h.reenviarAlmismosocket("#red#*No permitido*");
            return;
        }
        for (HiloDeCliente cliente : HiloDeCliente.conectados) {
            long currenttime = System.currentTimeMillis() - cliente.inisesion;

            StringBuilder estadisticas = new StringBuilder("*" + cliente.idserver + ":* " + cliente.correo
                    + " *Tiempo:* " + currenttime
                    + " ms *Mensajes:* " + cliente.cantmsg + "\n");

            estadisticas.append(" *Interacciones con otros usuarios:* \n");
            for (HiloDeCliente otroCliente : HiloDeCliente.conectados) {
                if (!otroCliente.idserver.equals(cliente.idserver)) {
                    int interacciones = cliente.obtenerInteraccionConUsuario(otroCliente.idserver);
                    estadisticas.append("   - Con ").append(otroCliente.correo)
                            .append(": ").append(interacciones).append(" mensajes\n");
                }
            }

            h.reenviarAlmismosocket(estadisticas.toString());
        }
    }
}
