package pe.inkacuida.api.dto;

import java.time.LocalDate;
import java.util.List;
import pe.inkacuida.api.dominio.Frecuencia;
import pe.inkacuida.api.dominio.Medicamento;

public record MedicamentoDto(Long id, String nombre, String dosis, Frecuencia frecuencia, List<String> horarios,
                             LocalDate fechaInicio, LocalDate fechaFin, String indicaciones,
                             Integer unidadesDisponibles, boolean activo) {

    public static MedicamentoDto de(Medicamento m) {
        return new MedicamentoDto(m.getId(), m.getNombre(), m.getDosis(), m.getFrecuencia(), List.copyOf(m.getHorarios()),
                m.getFechaInicio(), m.getFechaFin(), m.getIndicaciones(), m.getUnidadesDisponibles(), m.isActivo());
    }
}
