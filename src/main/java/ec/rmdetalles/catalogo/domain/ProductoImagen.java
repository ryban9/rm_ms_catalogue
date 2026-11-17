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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "producto_imagenes", schema = "rm_detalles")
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false, updatable = false)
    private Negocio negocio;

    @Column(name = "ruta_archivo", nullable = false, columnDefinition = "text")
    private String rutaStorage;

    @Column(name = "texto_alternativo", length = 180)
    private String textoAlternativo;

    @Column(nullable = false)
    private short orden = 0;

    @Column(name = "es_principal", nullable = false)
    private boolean principal = false;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    protected ProductoImagen() {
    }

    public ProductoImagen(
            Producto producto,
            String rutaStorage,
            String textoAlternativo,
            short orden,
            boolean principal
    ) {
        this.producto = producto;
        this.negocio = producto.getNegocio();
        this.rutaStorage = rutaStorage;
        this.textoAlternativo = textoAlternativo;
        this.orden = orden;
        this.principal = principal;
    }

    public UUID getId() { return id; }
    public Producto getProducto() { return producto; }
    public String getRutaStorage() { return rutaStorage; }
    public String getTextoAlternativo() { return textoAlternativo; }
    public short getOrden() { return orden; }
    public boolean isPrincipal() { return principal; }
    public void marcarPrincipal() { this.principal = true; }
    public void quitarPrincipal() { this.principal = false; }
    public void cambiarOrden(short orden) { this.orden = orden; }
    public OffsetDateTime getCreadoEn() { return creadoEn; }
}
