package labsd;

import java.util.ArrayList;

public class GrupoHandler {
    private HiloDeCliente h;

    public GrupoHandler(HiloDeCliente h) {
        this.h = h;
    }

    public void crearGrupo(String texto, String[] textsplitted) {
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/create") && textsplitted[i + 1].equals("group")) {
                ArrayList<String> integrantes = new ArrayList<>();
                integrantes.add(this.h.correo);
                this.h.db.creategroup(textsplitted[i + 2], integrantes);
                this.h.reenviarAlmismosocket("*#magenta#Grupo " + textsplitted[i + 2] + " creado\n");
                HiloDeCliente.groups = this.h.db.getallgroups();
                break;
            }
        }
    }

    public void unirseAGrupo(String texto, String[] textsplitted) {
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/join") && textsplitted[i + 1].equals("group")) {
                ArrayList<String> integrantes = this.h.db.getintegrantes(textsplitted[i + 2]);
                this.h.db.insertuser(textsplitted[i + 2], this.h.correo);
                for (String integrante : integrantes) {
                    for (HiloDeCliente h : HiloDeCliente.conectados) {
                        if (h.correo.equalsIgnoreCase(integrante)) {
                            h.reenviarAlmismosocket("*#green#" + this.h.nombre + " Se ha unido al grupo: " + textsplitted[i + 2]);
                        }
                    }
                }
                this.h.reenviarAlmismosocket("*#green#Unido al grupo: " + textsplitted[i + 2]);
                HiloDeCliente.groups = this.h.db.getallgroups();
                break;
            }
        }
    }

    public void abandonarGrupo(String texto, String[] textsplitted) {
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/leave") && textsplitted[i + 1].equals("group")) {
                ArrayList<String> integrantes = this.h.db.getintegrantes(textsplitted[i + 2]);
                for (int k = 0; k < integrantes.size(); k++) {
                    if (this.h.correo.equalsIgnoreCase(integrantes.get(k))) {
                        this.h.reenviarAlmismosocket("*#red#GRUPO " + textsplitted[i + 2] + " ABANDONADO\n");
                        integrantes.remove(k);
                        this.h.db.deleteusergroup(textsplitted[i + 2], this.h.correo);
                        break;
                    }
                }
                HiloDeCliente.groups = this.h.db.getallgroups();
                break;
            }
        }
    }

    public void enviarMensajeAGrupo(String texto, String[] textsplitted, String origin) {
        String auxtext = "";
        for (int i = 0; i < textsplitted.length; i++) {
            if (textsplitted[i].equals("/group")) {
                for (int j = i + 2; j < textsplitted.length; j++) {
                    auxtext += " " + textsplitted[j];
                }
                ArrayList<String> integrantes = this.h.db.getintegrantes(textsplitted[i + 1]);
                boolean entrar = integrantes.contains(this.h.correo);
                for (HiloDeCliente h : HiloDeCliente.conectados) {
                    if (h.idserver.equalsIgnoreCase(origin) || h.correo.equalsIgnoreCase(origin) || h.nombre.equalsIgnoreCase(origin)) {
                        origin = h.nombre;
                    }
                }
                this.h.db.insertmsg(textsplitted[i + 1], "*#magenta#[FROM GROUP " + textsplitted[i + 1] + " BY " + origin + "]:* " + "#black#" + auxtext);
                for (String integrante : integrantes) {
                    for (HiloDeCliente h : HiloDeCliente.conectados) {
                        if (h.correo.equalsIgnoreCase(integrante)) {
                            h.reenviarAlmismosocket("*#magenta#[FROM GROUP " + textsplitted[i + 1] + " BY " + origin + "]:* " + "#black#" + auxtext);
                        }
                    }
                }
                ArrayList<String> mensajes = this.h.db.getmensajes(textsplitted[i + 1]);
                if (!mensajes.isEmpty()) {
                    this.h.reenviarAlmismosocket("#orange#Use */msgs <group>* para ver los mensajes del grupo");
                }
                break;
            }
        }
    }

    public void mostrarTodosLosGrupos() {
        this.h.reenviarAlmismosocket("#magenta#*Grupos Creados:*\n---------------------");
        ArrayList<ArrayList> grupos = this.h.db.getallgroups();
        for (ArrayList grupo : grupos) {
            this.h.reenviarAlmismosocket("*#magenta#Nombre Grupo:* " + grupo.get(0) + "\n* Integrantes:*");
            ArrayList<String> integrantes = (ArrayList<String>) grupo.get(1);
            for (String integrante : integrantes) {
                this.h.reenviarAlmismosocket("  *#magenta# >" + integrante + "\n");
            }
        }
    }

    public void cargarmsggroup(String[] textsplitted){
        for (int i=0; i<textsplitted.length; i++) {
            if (textsplitted[i].equals("/msgs")) {
                ArrayList<String> mensajes = this.h.db.getmensajes(textsplitted[i + 1]);
                for (String s : mensajes) {
                    this.h.reenviarAlmismosocket(s);
                }
            }
        }
    }
}