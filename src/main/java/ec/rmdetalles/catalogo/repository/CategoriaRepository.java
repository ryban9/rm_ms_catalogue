package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    List<Categoria> findAllByOrderByNombreAsc();
    List<Categoria> findByActivaTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, UUID id);
}
