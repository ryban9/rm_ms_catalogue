package ec.rmdetalles.catalogo.api;

import ec.rmdetalles.catalogo.service.CategoriaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo/categorias")
public class CatalogoCategoriaController {
    private final CategoriaService service;
    public CatalogoCategoriaController(CategoriaService service) { this.service = service; }

    @GetMapping
    public List<CategoriaResponse> listar() { return service.listar(true); }
}
