package pe.inkacuida.api.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import pe.inkacuida.api.dto.ErrorDto;
import pe.inkacuida.api.servicio.ApiException;

/** Todas las respuestas de error tienen la forma { "mensaje": "..." }. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDto> api(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorDto(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> validacion(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(FieldError::getDefaultMessage).orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(new ErrorDto(mensaje));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorDto> validacionMetodo(HandlerMethodValidationException e) {
        String mensaje = e.getAllErrors().stream().findFirst()
                .map(err -> err.getDefaultMessage()).orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(new ErrorDto(mensaje));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
                       MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDto> formato(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorDto("Los datos enviados no tienen el formato correcto"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> integridad(DataIntegrityViolationException e) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto("El registro ya existe o entra en conflicto con otro"));
    }
}
