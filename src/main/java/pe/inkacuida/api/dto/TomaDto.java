package pe.inkacuida.api.dto;

import java.time.Instant;
import java.time.LocalDate;
import pe.inkacuida.api.dominio.EstadoToma;
import pe.inkacuida.api.dominio.Frecuencia;
import pe.inkacuida.api.dominio.Medicamento;
import pe.inkacuida.api.dominio.Toma;

public record TomaDto(Long id, Long medicamentoId, String medicamentoNombre, String dosis, String indicaciones,
                      Frecuencia frecuencia, LocalDate fecha, String hora, EstadoToma estado,
                      Instant respondidaEn, Instant pospuestaHasta) {

    public static TomaDto de(Toma t) {
        Medicamento m = t.getMedicamento();
        return new TomaDto(t.getId(), m.getId(), m.getNombre(), m.getDosis(), m.getIndicaciones(), m.getFrecuencia(),
                t.getFecha(), t.getHora(), t.getEstado(), t.getRespondidaEn(), t.getPospuestaHasta());
    }
}
