package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findAllByOrderByNombreAsc();
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, UUID id);

    @Query("""
        select p from Producto p
        where p.publicado = true and p.activo = true
          and (p.categoria is null or p.categoria.activa = true)
          and (:categoriaId is null or p.categoria.id = :categoriaId)
        order by p.nombre asc
        """)
    List<Producto> listarCatalogo(@Param("categoriaId") UUID categoriaId);

    Optional<Producto> findByIdAndPublicadoTrueAndActivoTrue(UUID id);
}
