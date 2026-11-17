package ec.rmdetalles.catalogo.service;

import ec.rmdetalles.catalogo.api.CategoriaRequest;
import ec.rmdetalles.catalogo.api.CategoriaResponse;
import ec.rmdetalles.catalogo.config.NegocioContext;
import ec.rmdetalles.catalogo.domain.Categoria;
import ec.rmdetalles.catalogo.repository.CategoriaRepository;
import ec.rmdetalles.catalogo.repository.NegocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaService {
    private final CategoriaRepository repository;
    private final NegocioRepository negocios;

    public CategoriaService(CategoriaRepository repository, NegocioRepository negocios) {
        this.repository = repository;
        this.negocios = negocios;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(boolean soloActivas) {
        UUID negocioId = NegocioContext.getRequired();
        List<Categoria> items = soloActivas
                ? repository.findByNegocio_IdAndActivaTrueOrderByNombreAsc(negocioId)
                : repository.findByNegocio_IdOrderByNombreAsc(negocioId);
        return items.stream().map(CategoriaResponse::from).toList();
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        String nombre = request.nombre().trim();
        if (repository.existsByNegocio_IdAndNombreIgnoreCase(negocioId, nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = new Categoria(negocios.getReferenceById(negocioId), nombre, request.descripcion());
        return CategoriaResponse.from(repository.save(categoria));
    }

    public CategoriaResponse actualizar(UUID id, CategoriaRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        Categoria categoria = buscar(id, negocioId);
        String nombre = request.nombre().trim();
        if (repository.existsByNegocio_IdAndNombreIgnoreCaseAndIdNot(negocioId, nombre, id)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        categoria.actualizar(nombre, request.descripcion(), request.activa() == null ? categoria.isActiva() : request.activa());
        return CategoriaResponse.from(repository.save(categoria));
    }

    public void desactivar(UUID id) {
        Categoria categoria = buscar(id, NegocioContext.getRequired());
        categoria.actualizar(categoria.getNombre(), categoria.getDescripcion(), false);
        repository.save(categoria);
    }

    private Categoria buscar(UUID id, UUID negocioId) {
        return repository.findByIdAndNegocio_Id(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría."));
    }
}
