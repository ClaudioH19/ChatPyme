package labsd;

import java.util.ArrayList;

public class MensajePrivadoHandler {
    private HiloDeCliente h;
    public static ArrayList<String[][]> users;

    public MensajePrivadoHandler(HiloDeCliente h) {
        this.h = h;
    }

    public void manejarMensajePrivado(String texto, String[] textsplitted, String idDest, String origin) {
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

        ArrayList<String[][]> users = h.db.getallusers();
        boolean conectado=true;
        for (String[][] s : users) {
            if (!h.db.readstatus(s[1][1])) {
                h.db.addmensajes(s[1][1], "*#magenta#[PRIVATE FROM " + this.h.nombre + " ]:* " + "#black#" + auxtext.trim());
                System.out.println("Mensaje guardado en pendiente");
                conectado=false;
            }
        }

        if(conectado) {
            for (HiloDeCliente h: HiloDeCliente.conectados) {
                if (h.correo.equals(idDest) || h.nombre.equals(idDest)) {
                    h.reenviarAlmismosocket("*#magenta#[PRIVATE FROM " + this.h.nombre + " ]:* " + "#black#" + auxtext.trim());
                    break; // Evita enviar múltiples veces si encuentra más de un destinatario
                }
            }
        }

        // Registrar que el mensaje fue enviado, sin duplicar
        this.h.reenviarAlmismosocket("*#magenta#[PRIVATE TO *" + idDest + "]:* " + "#black#" + auxtext.trim());
    }
}
