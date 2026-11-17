package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoImagenRepository
        extends JpaRepository<ProductoImagen, UUID> {

    List<ProductoImagen> findByProducto_IdAndNegocio_IdOrderByOrdenAsc(
            UUID productoId, UUID negocioId);

    long countByProducto_IdAndNegocio_Id(UUID productoId, UUID negocioId);

    boolean existsByProducto_IdAndNegocio_IdAndPrincipalTrue(UUID productoId, UUID negocioId);

    Optional<ProductoImagen> findByIdAndProducto_IdAndNegocio_Id(
            UUID imagenId, UUID productoId, UUID negocioId);
}
