package pe.inkacuida.api.dominio;

import jakarta.persistence.*;

/** Familiar o cuidador autorizado por el paciente para ver su cumplimiento. */
@Entity
@Table(name = "cuidador")
public class Cuidador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 60)
    private String parentesco;

    @Column(name = "recibe_alertas", nullable = false)
    private boolean recibeAlertas;

    protected Cuidador() {}

    public Cuidador(Usuario usuario, String nombre, String email, String parentesco, boolean recibeAlertas) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.email = email;
        this.parentesco = parentesco;
        this.recibeAlertas = recibeAlertas;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getParentesco() { return parentesco; }
    public boolean isRecibeAlertas() { return recibeAlertas; }
}
