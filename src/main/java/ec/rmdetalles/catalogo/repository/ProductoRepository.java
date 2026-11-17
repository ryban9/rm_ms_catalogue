package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    List<Producto> findByNegocio_IdOrderByNombreAsc(UUID negocioId);
    boolean existsByNegocio_IdAndCodigoIgnoreCase(UUID negocioId, String codigo);
    boolean existsByNegocio_IdAndCodigoIgnoreCaseAndIdNot(UUID negocioId, String codigo, UUID id);

    @Query("""
        select p from Producto p left join p.categoria c
        where p.negocio.id = :negocioId and p.publicado = true and p.activo = true
          and (p.categoria is null or c.activa = true)
          and (:categoriaId is null or c.id = :categoriaId)
        order by p.nombre asc
        """)
    List<Producto> listarCatalogo(@Param("negocioId") UUID negocioId, @Param("categoriaId") UUID categoriaId);

    Optional<Producto> findByIdAndNegocio_IdAndPublicadoTrueAndActivoTrue(UUID id, UUID negocioId);
    Optional<Producto> findByIdAndNegocio_Id(UUID id, UUID negocioId);
}
