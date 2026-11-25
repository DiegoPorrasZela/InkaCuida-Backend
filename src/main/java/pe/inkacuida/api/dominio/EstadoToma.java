package pe.inkacuida.api.dominio;

/**
 * Estado de una toma. Una toma sin respuesta permanece PENDIENTE:
 * la falta de confirmación nunca se convierte automáticamente en OMITIDA.
 */
public enum EstadoToma {
    PENDIENTE, TOMADA, OMITIDA
}
