package pe.inkacuida.api.servicio;

import org.springframework.http.HttpStatus;

/** Error de negocio con código HTTP y mensaje legible para el usuario. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static ApiException noEncontrado(String mensaje) { return new ApiException(HttpStatus.NOT_FOUND, mensaje); }
    public static ApiException invalido(String mensaje) { return new ApiException(HttpStatus.BAD_REQUEST, mensaje); }
    public static ApiException conflicto(String mensaje) { return new ApiException(HttpStatus.CONFLICT, mensaje); }
}
