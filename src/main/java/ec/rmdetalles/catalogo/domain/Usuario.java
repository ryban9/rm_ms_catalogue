package ec.rmdetalles.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "rm_detalles")
public class Usuario {
    @Id
    private UUID id; // UUID de auth.users (claim sub del JWT)

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String rol;

    @Column(nullable = false)
    private boolean activo;

    protected Usuario() { }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
}
