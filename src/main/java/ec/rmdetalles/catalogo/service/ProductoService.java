package ec.rmdetalles.catalogo.service;

import ec.rmdetalles.catalogo.api.ProductoImagenResponse;
import ec.rmdetalles.catalogo.api.ProductoRequest;
import ec.rmdetalles.catalogo.api.ProductoResponse;
import ec.rmdetalles.catalogo.domain.Categoria;
import ec.rmdetalles.catalogo.domain.Producto;
import ec.rmdetalles.catalogo.domain.ProductoImagen;
import ec.rmdetalles.catalogo.repository.CategoriaRepository;
import ec.rmdetalles.catalogo.repository.ProductoImagenRepository;
import ec.rmdetalles.catalogo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final ProductoImagenRepository imagenes;
    private final StorageService storage;
    private final String supabaseUrl;

    public ProductoService(
            ProductoRepository productos,
            CategoriaRepository categorias,
            ProductoImagenRepository imagenes,
            StorageService storage,
            @Value("${app.supabase.url}") String supabaseUrl
    ) {
        this.productos = productos;
        this.categorias = categorias;
        this.imagenes = imagenes;
        this.storage = storage;
        this.supabaseUrl = supabaseUrl;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAdministracion() {
        return productos.findAllByOrderByNombreAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarCatalogo(UUID categoriaId) {
        return productos.listarCatalogo(categoriaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPublicado(UUID id) {
        Producto producto = productos.findByIdAndPublicadoTrueAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto publicado."));
        return toResponse(producto);
    }

    public ProductoResponse crear(ProductoRequest request) {
        String codigo = request.codigo().trim();
        if (productos.existsByCodigoIgnoreCase(codigo)) throw new IllegalArgumentException("Ya existe un producto con ese código.");
        Categoria categoria = resolverCategoria(request.categoriaId());
        Producto producto = new Producto(categoria, codigo, request.nombre().trim(), request.descripcion(),
                request.precioReferencial(), bool(request.publicado(), false), bool(request.personalizable(), false),
                bool(request.controlaStock(), true));
        Producto guardado = productos.save(producto);
        return toResponse(guardado);
    }

    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        Producto producto = productos.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));
        String codigo = request.codigo().trim();
        if (productos.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) throw new IllegalArgumentException("Ya existe un producto con ese código.");
        producto.actualizar(resolverCategoria(request.categoriaId()), codigo, request.nombre().trim(), request.descripcion(),
                request.precioReferencial(), bool(request.publicado(), false), bool(request.personalizable(), false),
                bool(request.controlaStock(), true), bool(request.activo(), producto.isActivo()));
        Producto guardado = productos.save(producto);
        return toResponse(guardado);
    }

    public ProductoImagenResponse subirImagen(
            UUID productoId,
            MultipartFile archivo,
            String textoAlternativo
    ) {
        Producto producto = productos.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen.");
        }

        if (archivo.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no puede superar 5 MB.");
        }

        String mime = archivo.getContentType();
        if (mime == null || !List.of("image/jpeg", "image/png", "image/webp").contains(mime.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WebP.");
        }

        long total = imagenes.countByProducto_Id(productoId);
        if (total >= 5) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto ya alcanzó el máximo de 5 imágenes."
            );
        }

        String extension = switch (mime.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato no permitido.");
        };

        String ruta = productoId + "/" + UUID.randomUUID() + "." + extension;

        try {
            storage.subir(ruta, archivo.getBytes(), mime);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.");
        }

        boolean principal = total == 0;
        short orden = (short) total;

        ProductoImagen imagen = new ProductoImagen(
                producto, ruta, textoAlternativo, orden, principal
        );

        ProductoImagen guardada = imagenes.save(imagen);
        return ProductoImagenResponse.from(guardada, supabaseUrl);
    }

    public void eliminarImagen(UUID productoId, UUID imagenId) {
        ProductoImagen imagen = imagenes.findByIdAndProducto_Id(imagenId, productoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No se encontró esa imagen para el producto."));

        storage.eliminar(imagen.getRutaStorage());

        boolean eraPrincipal = imagen.isPrincipal();
        imagenes.delete(imagen);

        List<ProductoImagen> restantes =
                imagenes.findByProducto_IdOrderByOrdenAsc(productoId);

        for (int i = 0; i < restantes.size(); i++) {
            ProductoImagen restante = restantes.get(i);
            restante.cambiarOrden((short) i);

            if (i == 0) {
                restante.marcarPrincipal();
            } else if (restante.isPrincipal()) {
                restante.quitarPrincipal();
            }
        }

        imagenes.saveAll(restantes);
    }

    public void desactivar(UUID id) {
        Producto producto = productos.findById(id).orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));
        producto.desactivar();
        productos.save(producto);
    }

    private Categoria resolverCategoria(UUID categoriaId) {
        if (categoriaId == null) return null;
        Categoria categoria = categorias.findById(categoriaId).orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría indicada."));
        if (!categoria.isActiva()) throw new IllegalArgumentException("No se puede asociar un producto a una categoría inactiva.");
        return categoria;
    }

    private boolean bool(Boolean value, boolean defaultValue) { return value == null ? defaultValue : value; }

    private ProductoResponse toResponse(Producto producto) {
        List<ProductoImagen> imagenesProducto =
                imagenes.findByProducto_IdOrderByOrdenAsc(producto.getId());

        return ProductoResponse.from(producto, imagenesProducto, supabaseUrl);
    }
}
