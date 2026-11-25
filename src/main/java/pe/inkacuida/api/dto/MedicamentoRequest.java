package pe.inkacuida.api.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import pe.inkacuida.api.dominio.Frecuencia;

public record MedicamentoRequest(
        @NotBlank(message = "El nombre del medicamento es obligatorio") @Size(max = 120) String nombre,
        @NotBlank(message = "La dosis prescrita es obligatoria") @Size(max = 60) String dosis,
        @NotNull(message = "La frecuencia es obligatoria") Frecuencia frecuencia,
        @NotEmpty(message = "Debes registrar al menos un horario")
        List<@NotNull @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Los horarios deben tener el formato HH:mm") String> horarios,
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
        LocalDate fechaFin,
        @Size(max = 500, message = "Las indicaciones no deben superar 500 caracteres") String indicaciones,
        @PositiveOrZero(message = "Las unidades disponibles no pueden ser negativas") Integer unidadesDisponibles) {}
