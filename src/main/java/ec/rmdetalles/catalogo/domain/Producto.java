package ec.rmdetalles.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "productos", schema = "rm_detalles")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false, updatable = false)
    private Negocio negocio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(name = "precio_referencial", precision = 12, scale = 2)
    private BigDecimal precioReferencial;

    @Column(nullable = false)
    private boolean publicado = false;

    @Column(nullable = false)
    private boolean personalizable = false;

    @Column(name = "controla_stock", nullable = false)
    private boolean controlaStock = true;

    @Column(nullable = false)
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    protected Producto() { }

    public Producto(Negocio negocio, Categoria categoria, String codigo, String nombre, String descripcion,
                    BigDecimal precioReferencial, boolean publicado,
                    boolean personalizable, boolean controlaStock) {
        this.negocio = negocio;
        this.categoria = categoria;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioReferencial = precioReferencial;
        this.publicado = publicado;
        this.personalizable = personalizable;
        this.controlaStock = controlaStock;
        this.activo = true;
    }

    public UUID getId() { return id; }
    public Negocio getNegocio() { return negocio; }
    public Categoria getCategoria() { return categoria; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecioReferencial() { return precioReferencial; }
    public boolean isPublicado() { return publicado; }
    public boolean isPersonalizable() { return personalizable; }
    public boolean isControlaStock() { return controlaStock; }
    public boolean isActivo() { return activo; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }

    public void actualizar(Categoria categoria, String codigo, String nombre, String descripcion,
                           BigDecimal precioReferencial, boolean publicado,
                           boolean personalizable, boolean controlaStock, boolean activo) {
        this.categoria = categoria;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioReferencial = precioReferencial;
        this.publicado = publicado;
        this.personalizable = personalizable;
        this.controlaStock = controlaStock;
        this.activo = activo;
    }

    public void desactivar() {
        this.activo = false;
        this.publicado = false;
    }
}
