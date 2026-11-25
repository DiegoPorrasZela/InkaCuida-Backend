package pe.inkacuida.api.dominio;

import java.time.ZoneId;

/** Constantes de las reglas del MVP (informe, sección 3.3). */
public final class ReglasNegocio {
    private ReglasNegocio() {}

    /** Zona horaria en la que se calcula "hoy". */
    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    /** Minutos sin confirmación tras los cuales corresponde alertar al cuidador autorizado (HU02). */
    public static final int UMBRAL_ALERTA_CUIDADOR_MIN = 30;

    /** Días estimados restantes a partir de los cuales se alerta la reposición (HU03). */
    public static final int UMBRAL_REPOSICION_DIAS = 5;

    /** Minutos que se pospone un recordatorio. */
    public static final int MINUTOS_POSPONER = 30;
}
