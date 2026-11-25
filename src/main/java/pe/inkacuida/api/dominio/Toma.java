package pe.inkacuida.api.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Una dosis programada (medicamento + fecha + hora) y la respuesta del paciente. */
@Entity
@Table(name = "toma",
       uniqueConstraints = @UniqueConstraint(name = "uk_toma_med_fecha_hora", columnNames = {"medicamento_id", "fecha", "hora"}),
       indexes = @Index(name = "ix_toma_usuario_fecha", columnList = "usuario_id, fecha"))
public class Toma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 5)
    private String hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoToma estado = EstadoToma.PENDIENTE;

    @Column(name = "respondida_en")
    private Instant respondidaEn;

    @Column(name = "pospuesta_hasta")
    private Instant pospuestaHasta;

    protected Toma() {}

    public Toma(Medicamento medicamento, LocalDate fecha, String hora) {
        this.usuario = medicamento.getUsuario();
        this.medicamento = medicamento;
        this.fecha = fecha;
        this.hora = hora;
    }

    public boolean isPendiente() {
        return estado == EstadoToma.PENDIENTE;
    }

    public void confirmar(Instant ahora) {
        this.estado = EstadoToma.TOMADA;
        this.respondidaEn = ahora;
        this.pospuestaHasta = null;
        medicamento.descontarUnidad();
    }

    public void omitir(Instant ahora) {
        this.estado = EstadoToma.OMITIDA;
        this.respondidaEn = ahora;
        this.pospuestaHasta = null;
    }

    /** Posponer no cambia el estado: la toma sigue PENDIENTE. */
    public void posponer(Instant hasta) {
        this.pospuestaHasta = hasta;
    }

    /** Solo para datos de demostración. */
    public void registrarHistorico(EstadoToma estado, Instant respondidaEn) {
        this.estado = estado;
        this.respondidaEn = respondidaEn;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Medicamento getMedicamento() { return medicamento; }
    public LocalDate getFecha() { return fecha; }
    public String getHora() { return hora; }
    public EstadoToma getEstado() { return estado; }
    public Instant getRespondidaEn() { return respondidaEn; }
    public Instant getPospuestaHasta() { return pospuestaHasta; }
}
