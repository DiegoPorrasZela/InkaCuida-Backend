package pe.inkacuida.api.dto;

import pe.inkacuida.api.dominio.Cuidador;

public record CuidadorDto(Long id, String nombre, String email, String parentesco, boolean recibeAlertas) {

    public static CuidadorDto de(Cuidador c) {
        return new CuidadorDto(c.getId(), c.getNombre(), c.getEmail(), c.getParentesco(), c.isRecibeAlertas());
    }
}
