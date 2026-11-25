package pe.inkacuida.api.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(length = 80)
    private String ciudad;

    @Column(name = "consentimiento_aceptado", nullable = false)
    private boolean consentimientoAceptado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    protected Usuario() {}

    public Usuario(String nombre, String email, String passwordHash, boolean consentimientoAceptado) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.consentimientoAceptado = consentimientoAceptado;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public boolean isConsentimientoAceptado() { return consentimientoAceptado; }
    public Instant getCreadoEn() { return creadoEn; }
}
