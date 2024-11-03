package labsd;

import com.mongodb.MongoException;
import java.util.ArrayList;

public class Comandos {
    private HiloDeCliente h;
    private MensajePrivadoHandler mensajePrivadoHandler;
    private GrupoHandler grupoHandler;
    private UsuarioHandler usuarioHandler;
    private SistemaHandler sistemaHandler;

    public Comandos(HiloDeCliente h) {
        this.h = h;
        this.mensajePrivadoHandler = new MensajePrivadoHandler(h);
        this.grupoHandler = new GrupoHandler(h);
        this.usuarioHandler = new UsuarioHandler(h);
        this.sistemaHandler = new SistemaHandler(h);
    }

    public void comandos(String texto, String[] textsplitted, String idDest, String origin) throws Throwable {
        if (texto.contains("/private") && validationrol()) {
            mensajePrivadoHandler.manejarMensajePrivado(texto, textsplitted, idDest, origin);
        } else if (texto.contains("/create group") && validationrol()) {
            grupoHandler.crearGrupo(texto, textsplitted);
        } else if (texto.contains("/join group") && validationrol()) {
            grupoHandler.unirseAGrupo(texto, textsplitted);
        } else if (texto.contains("/leave group") && validationrol()) {
            grupoHandler.abandonarGrupo(texto, textsplitted);
        } else if (texto.contains("/group") && validationrol()) {
            grupoHandler.enviarMensajeAGrupo(texto, textsplitted, origin);
        } else if (texto.contains("/allgroups") && validationrol()) {
            grupoHandler.mostrarTodosLosGrupos();
        } else if (texto.contains("/status") && validationrol()) {
            sistemaHandler.mostrarEstado();
        } else if (texto.contains("/login")) {
            usuarioHandler.iniciarSesion(texto, textsplitted);
        } else if (texto.contains("/newpassword")) {
            usuarioHandler.cambiarContrasena(texto, textsplitted);
        } else if (texto.contains("/read") && validationrol()) {
            usuarioHandler.leerMensajesPendientes();
        } else if (texto.contains("/register") && validationrol()) {
            usuarioHandler.registrarUsuario(texto, textsplitted);
        } else if (texto.contains("/help") && validationrol()) {
            sistemaHandler.mostrarAyuda();
        } else if (texto.contains("/admin") && validationrol()) {
            sistemaHandler.enviarMensajeAAdministradores(texto, textsplitted);
        } else if (texto.contains("/aux") && validationrol()) {
            sistemaHandler.enviarMensajeAAuxiliares(texto, textsplitted);
        } else if (texto.contains("/urgency") && validationrol()) {
            sistemaHandler.enviarMensajeDeEmergencia(texto, textsplitted);
        } else if (texto.contains("/stats")) {
            sistemaHandler.mostrarEstadisticas();
        } else if (texto.contains("/exit")) {
            sistemaHandler.desconectarCliente(origin);
        }
    }

    public boolean validationrol() {
        if (this.h.ingreso == 0) {
            this.h.reenviarAlmismosocket("#red#NECESITA UNA NUEVA CONTRASEÑA, USE */newpassword <clave>");
            return false;
        }
        if (this.h.rol == null || this.h.rol.isEmpty()) {
            this.h.reenviarAlmismosocket("#blue#NECESITA INGRESAR AL SISTEMA.");
            return false;
        }
        return true;
    }
}
