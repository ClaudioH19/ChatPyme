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
            h.reenviarAlmismosocket("#red#Cierre sesión primero */exit*");
            return;
        }

        for (int i = 0; i < textsplitted.length; i++) {
            String correo="default";
            String clave="null";
            if (textsplitted[i].equals("/login")) {
                if(i+1<textsplitted.length)
                    correo = textsplitted[i + 1];
                if(i+2<textsplitted.length)
                    clave = textsplitted[i + 2];

                String[][] user = h.db.readuser(correo);
                if (user == null) {
                    h.reenviarAlmismosocket("*#red#USUARIO NO ENCONTRADO");
                    return;
                }

                if (!user[3][1].equals(clave)) {
                    h.reenviarAlmismosocket("*#red#CONTRASEÑA INCORRECTA");
                    return;
                }

                int rem=-1;
                int idx=0;
                for (HiloDeCliente h: HiloDeCliente.conectados){
                    if(h.correo.equals(correo) && h.db.readstatus(correo)){
                        //this.h.reenviarAlmismosocket("#red#Usuario ya se encuentra en línea");
                        //return;
                        rem=idx;
                    }
                    idx++;
                }
                if(rem>=0 && !HiloDeCliente.conectados.isEmpty())
                    HiloDeCliente.conectados.remove(rem);

                h.nombre = user[0][1];
                h.correo = user[1][1];
                h.rut = user[2][1];
                h.clave = user[3][1];
                h.rol = user[4][1];
                h.ingreso = Integer.parseInt(user[5][1]);

                h.reenviarAlmismosocket("*#magenta#Logueado como:* " + h.nombre + " <" + h.rol + ">");
                h.reenviarAlmismosocket("/loggedsucceed "+ h.idserver +" "+h.correo); //esto servira para enviar una bandera de que se ha logueado, de paso enviamos su correo
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
                    if(!cliente.correo.equals(h.correo)){}
                        cliente.reenviarAlmismosocket("#magenta#" + h.nombre + String.format(" *<%s>* ", h.rol) + " Connected");
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
                h.reenviarAlmismosocket("*#magenta# Contraseña modificada");
                break;
            }
        }
    }

    public void leerMensajesPendientes() {
        h.reenviarAlmismosocket("*#orange#Mostrando no leídos...");
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
                    String nombre="default",correo="default",rut="default",clave="default",rol="auxiliar";

                    if(i+1<textsplitted.length)
                        nombre = textsplitted[i + 1];
                    if(i+2<textsplitted.length)
                        correo = textsplitted[i + 2];
                    if(i+3<textsplitted.length)
                        rut = textsplitted[i + 3];
                    if(i+4<textsplitted.length)
                        clave = textsplitted[i + 4];
                    if(i+5<textsplitted.length)
                        rol = textsplitted[i + 5].toLowerCase();

                    if (rol.equalsIgnoreCase("medico")) {
                        rol = "médico";
                    }
                    if(rol.equalsIgnoreCase("examenes")) {
                        rol = "exámenes";
                    }
                    if(rol.equalsIgnoreCase("pabellon"))
                        rol = "pabellón";
                    if(rol.equalsIgnoreCase("admision"))
                        rol = "admisión";

                    if (!rol.matches("^(médico|exámenes|pabellón|admisión|auxiliar)$")) {
                        h.reenviarAlmismosocket("#red#Rol *no válido*. Solo se permiten *'médico'*, *'exámenes'*, *'pabellón'*, *'admisión'* o *'auxiliar*'.");
                        return;
                    }

                    h.db.createuser(nombre, correo, rut, clave, rol);
                    h.reenviarAlmismosocket("#magenta#Usuario " + nombre + " creado");
                } catch (MongoException e) {
                    h.reenviarAlmismosocket(String.valueOf(e));
                }
                break;
            }
        }
    }

    public void verinfousuario(String origin){
        for (HiloDeCliente h : HiloDeCliente.conectados) {
            if (h.idserver.equalsIgnoreCase(origin)) {
                h.reenviarAlmismosocket("#magenta#Connected as " + h.nombre+String.format(" <%s> <%s>",this.h.rol,this.h.correo) + "\n");
            }
        }
    }

}
