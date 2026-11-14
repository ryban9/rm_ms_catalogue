package ec.rmdetalles.catalogo.service;

import ec.rmdetalles.catalogo.api.CategoriaRequest;
import ec.rmdetalles.catalogo.api.CategoriaResponse;
import ec.rmdetalles.catalogo.domain.Categoria;
import ec.rmdetalles.catalogo.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(boolean soloActivas) {
        List<Categoria> items = soloActivas ? repository.findByActivaTrueOrderByNombreAsc() : repository.findAllByOrderByNombreAsc();
        return items.stream().map(CategoriaResponse::from).toList();
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        Categoria categoria = new Categoria(nombre, request.descripcion());
        return CategoriaResponse.from(repository.save(categoria));
    }

    public CategoriaResponse actualizar(UUID id, CategoriaRequest request) {
        Categoria categoria = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría."));
        String nombre = request.nombre().trim();
        if (repository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        categoria.actualizar(nombre, request.descripcion(), request.activa() == null ? categoria.isActiva() : request.activa());
        return CategoriaResponse.from(repository.save(categoria));
    }

    public void desactivar(UUID id) {
        Categoria categoria = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría."));
        categoria.actualizar(categoria.getNombre(), categoria.getDescripcion(), false);
        repository.save(categoria);
    }
}
