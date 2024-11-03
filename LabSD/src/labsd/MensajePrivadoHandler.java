package labsd;

import java.util.ArrayList;

public class MensajePrivadoHandler {
    private HiloDeCliente h;
    public static ArrayList<String[][]> users;

    public MensajePrivadoHandler(HiloDeCliente h) {
        this.h = h;
    }

    public void manejarMensajePrivado(String texto, String[] textsplitted, String idDest, String origin) {
        if (texto.contains("/private")) {
            String auxtext = "";
            for (int i = 0; i < textsplitted.length; i++) {
                if (textsplitted[i].equals("/private")) {
                    idDest = textsplitted[i + 1];
                    for (int j = i + 2; j < textsplitted.length; j++) {
                        auxtext += " " + textsplitted[j];
                    }
                    break;
                }
            }

            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(idDest) || h.correo.equalsIgnoreCase(idDest) || h.nombre.equalsIgnoreCase(idDest)) {
                    h.reenviarAlmismosocket("#blue#[PRIVATE FROM *" + this.h.nombre + " *]: " + "#black#" + auxtext);
                    break;
                }
            }

            for (HiloDeCliente h : HiloDeCliente.conectados) {
                if (h.idserver.equalsIgnoreCase(idDest) || h.correo.equalsIgnoreCase(idDest) || h.nombre.equalsIgnoreCase(idDest)) {
                    idDest = h.nombre;
                }
            }

            for (String[][] s : users) {
                if ((s[0][1].equals(idDest) || s[1][1].equals(idDest)) && !this.h.db.readstatus(s[1][1])) {
                    this.h.db.addmensajes(s[1][1], "#blue#[PRIVATE FROM *" + this.h.nombre + " *]: " + "#black#" + auxtext);
                    break;
                }
            }

            this.h.reenviarAlmismosocket("#blue#[PRIVATE TO *" + idDest + "*]: " + "#black#" + auxtext);
        }
    }
}
