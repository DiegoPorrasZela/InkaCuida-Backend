package pe.inkacuida.api.dominio;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Entrada del carnet digital. Guarda la prescripción tal como la ingresó el paciente. */
@Entity
@Table(name = "medicamento")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String dosis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Frecuencia frecuencia;

    /** Horas en formato "HH:mm". */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicamento_horario", joinColumns = @JoinColumn(name = "medicamento_id"))
    @Column(name = "hora", length = 5, nullable = false)
    @OrderColumn(name = "orden")
    private List<String> horarios = new ArrayList<>();

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(length = 500)
    private String indicaciones;

    @Column(name = "unidades_disponibles")
    private Integer unidadesDisponibles;

    @Column(nullable = false)
    private boolean activo = true;

    protected Medicamento() {}

    public Medicamento(Usuario usuario) {
        this.usuario = usuario;
    }

    /** ¿El tratamiento está vigente en la fecha indicada? */
    public boolean vigenteEn(LocalDate fecha) {
        return activo && !fechaInicio.isAfter(fecha) && (fechaFin == null || !fechaFin.isBefore(fecha));
    }

    /** Descuenta una unidad al confirmar una toma (si el paciente registró unidades). */
    public void descontarUnidad() {
        if (unidadesDisponibles != null && unidadesDisponibles > 0) unidadesDisponibles--;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia frecuencia) { this.frecuencia = frecuencia; }
    public List<String> getHorarios() { return horarios; }
    public void setHorarios(List<String> nuevos) { this.horarios.clear(); this.horarios.addAll(nuevos); }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public Integer getUnidadesDisponibles() { return unidadesDisponibles; }
    public void setUnidadesDisponibles(Integer unidadesDisponibles) { this.unidadesDisponibles = unidadesDisponibles; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
