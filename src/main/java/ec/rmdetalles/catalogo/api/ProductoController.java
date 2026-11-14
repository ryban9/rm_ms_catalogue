package ec.rmdetalles.catalogo.api;

import ec.rmdetalles.catalogo.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class ProductoController {
    private final ProductoService service;
    public ProductoController(ProductoService service) { this.service = service; }

    @GetMapping("/api/v1/catalogo/productos")
    public List<ProductoResponse> listarCatalogo(@RequestParam(required = false) UUID categoriaId) {
        return service.listarCatalogo(categoriaId);
    }

    @GetMapping("/api/v1/catalogo/productos/{id}")
    public ProductoResponse obtenerPublico(@PathVariable UUID id) { return service.obtenerPublicado(id); }

    @GetMapping("/api/v1/productos")
    public List<ProductoResponse> listarAdministracion() {
        return service.listarAdministracion();
    }

    @PostMapping("/api/v1/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) { return service.crear(request); }

    @PostMapping(
            value = "/api/v1/productos/{id}/imagenes",
            consumes = "multipart/form-data"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoImagenResponse subirImagen(
            @PathVariable UUID id,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(required = false) String textoAlternativo
    ) {
        return service.subirImagen(id, archivo, textoAlternativo);
    }

    @PutMapping("/api/v1/productos/{id}")
    public ProductoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ProductoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/api/v1/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) { service.desactivar(id); }
}
