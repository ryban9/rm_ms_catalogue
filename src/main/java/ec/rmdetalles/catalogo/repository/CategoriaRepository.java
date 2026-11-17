package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findByNegocio_IdOrderByNombreAsc(UUID negocioId);
    List<Categoria> findByNegocio_IdAndActivaTrueOrderByNombreAsc(UUID negocioId);
    Optional<Categoria> findByIdAndNegocio_Id(UUID id, UUID negocioId);
    boolean existsByNegocio_IdAndNombreIgnoreCase(UUID negocioId, String nombre);
    boolean existsByNegocio_IdAndNombreIgnoreCaseAndIdNot(UUID negocioId, String nombre, UUID id);
}
