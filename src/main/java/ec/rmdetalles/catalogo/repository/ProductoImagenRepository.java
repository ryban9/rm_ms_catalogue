package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductoImagenRepository
        extends JpaRepository<ProductoImagen, UUID> {

    List<ProductoImagen> findByProducto_IdOrderByOrdenAsc(UUID productoId);

    long countByProducto_Id(UUID productoId);

    boolean existsByProducto_IdAndPrincipalTrue(UUID productoId);
}
