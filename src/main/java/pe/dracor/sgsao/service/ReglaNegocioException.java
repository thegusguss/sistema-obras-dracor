package pe.dracor.sgsao.service;

/** Error de validacion o regla de negocio: el mensaje se muestra al usuario. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
