package ec.rmdetalles.catalogo.service;

import ec.rmdetalles.catalogo.api.ProductoImagenResponse;
import ec.rmdetalles.catalogo.api.ProductoRequest;
import ec.rmdetalles.catalogo.api.ProductoResponse;
import ec.rmdetalles.catalogo.config.NegocioContext;
import ec.rmdetalles.catalogo.domain.Categoria;
import ec.rmdetalles.catalogo.domain.Producto;
import ec.rmdetalles.catalogo.domain.ProductoImagen;
import ec.rmdetalles.catalogo.repository.CategoriaRepository;
import ec.rmdetalles.catalogo.repository.ConfiguracionNegocioRepository;
import ec.rmdetalles.catalogo.repository.NegocioRepository;
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
    private final NegocioRepository negocios;
    private final ConfiguracionNegocioRepository configuraciones;
    private final ProductoImagenRepository imagenes;
    private final StorageService storage;
    private final String supabaseUrl;

    public ProductoService(
            ProductoRepository productos,
            CategoriaRepository categorias,
            NegocioRepository negocios,
            ConfiguracionNegocioRepository configuraciones,
            ProductoImagenRepository imagenes,
            StorageService storage,
            @Value("${app.supabase.url}") String supabaseUrl
    ) {
        this.productos = productos;
        this.categorias = categorias;
        this.negocios = negocios;
        this.configuraciones = configuraciones;
        this.imagenes = imagenes;
        this.storage = storage;
        this.supabaseUrl = supabaseUrl;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAdministracion() {
        return productos.findByNegocio_IdOrderByNombreAsc(NegocioContext.getRequired())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarCatalogo(UUID categoriaId) {
        return productos.listarCatalogo(NegocioContext.getRequired(), categoriaId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPublicado(UUID id) {
        Producto producto = productos.findByIdAndNegocio_IdAndPublicadoTrueAndActivoTrue(id, NegocioContext.getRequired())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto publicado."));
        return toResponse(producto);
    }

    public ProductoResponse crear(ProductoRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        String codigo = request.codigo().trim();
        if (productos.existsByNegocio_IdAndCodigoIgnoreCase(negocioId, codigo)) throw new IllegalArgumentException("Ya existe un producto con ese código.");
        Categoria categoria = resolverCategoria(request.categoriaId());
        Producto producto = new Producto(negocios.getReferenceById(negocioId), categoria, codigo, request.nombre().trim(), request.descripcion(),
                request.precioReferencial(), bool(request.publicado(), false), bool(request.personalizable(), false),
                bool(request.controlaStock(), true));
        Producto guardado = productos.save(producto);
        return toResponse(guardado);
    }

    public ProductoResponse actualizar(UUID id, ProductoRequest request) {
        UUID negocioId = NegocioContext.getRequired();
        Producto producto = productos.findByIdAndNegocio_Id(id, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));
        String codigo = request.codigo().trim();
        if (productos.existsByNegocio_IdAndCodigoIgnoreCaseAndIdNot(negocioId, codigo, id)) throw new IllegalArgumentException("Ya existe un producto con ese código.");
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
        UUID negocioId = NegocioContext.getRequired();
        Producto producto = productos.findByIdAndNegocio_Id(productoId, negocioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen.");
        }

        int maximoMb = obtenerEnteroConfiguracion(
                negocioId,
                "catalogo.imagenes.tamano_maximo_mb"
        );
        long maximoBytes = maximoMb * 1024L * 1024L;
        if (archivo.getSize() > maximoBytes) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "La imagen supera el máximo configurado de " + maximoMb + " MB."
            );
        }

        String mime = archivo.getContentType();
        List<String> mimePermitidos = configuraciones.buscarLista(
                negocioId,
                "catalogo.imagenes.mime_permitidos"
        ).stream()
                .map(tipo -> tipo.toLowerCase(Locale.ROOT))
                .toList();

        if (mimePermitidos.isEmpty()) {
            throw new IllegalStateException("No hay tipos MIME configurados para el negocio.");
        }

        if (mime == null || !mimePermitidos.contains(mime.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tipo de imagen no está permitido para este negocio."
            );
        }

        int maximoImagenes = obtenerMaximoImagenes(negocioId);
        long total = imagenes.countByProducto_IdAndNegocio_Id(productoId, negocioId);

        if (total >= maximoImagenes) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El producto ya alcanzó el máximo configurado de "
                            + maximoImagenes + " imágenes."
            );
        }

        String extension = switch (mime.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };

        String ruta = negocioId + "/" + productoId + "/" + UUID.randomUUID() + extension;

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
        UUID negocioId = NegocioContext.getRequired();
        ProductoImagen imagen = imagenes.findByIdAndProducto_IdAndNegocio_Id(imagenId, productoId, negocioId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No se encontró esa imagen para el producto."));

        storage.eliminar(imagen.getRutaStorage());

        imagenes.delete(imagen);

        List<ProductoImagen> restantes =
                imagenes.findByProducto_IdAndNegocio_IdOrderByOrdenAsc(productoId, negocioId);

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
        Producto producto = productos.findByIdAndNegocio_Id(id, NegocioContext.getRequired()).orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto."));
        producto.desactivar();
        productos.save(producto);
    }

    private int obtenerEnteroConfiguracion(UUID negocioId, String clave) {
        String valor = configuraciones.buscarValor(negocioId, clave)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Falta la configuración " + clave + " para el negocio."
                        )
                );

        try {
            int numero = Integer.parseInt(valor);
            if (numero <= 0 || numero > 10) {
                throw new IllegalStateException(
                        "La configuración " + clave + " debe estar entre 1 y 10."
                );
            }
            return numero;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "La configuración " + clave + " debe ser un entero."
            );
        }
    }

    private int obtenerMaximoImagenes(UUID negocioId) {
        String valor = configuraciones.buscarValor(
                negocioId,
                "catalogo.imagenes.max_por_producto"
        ).orElseThrow(() ->
                new IllegalStateException(
                        "No existe la configuración del máximo de imágenes para el negocio."
                )
        );

        try {
            int maximo = Integer.parseInt(valor);
            if (maximo < 0) {
                throw new IllegalStateException(
                        "El máximo configurado no puede ser negativo."
                );
            }
            return maximo;
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "El máximo configurado debe ser un número entero."
            );
        }
    }

    private Categoria resolverCategoria(UUID categoriaId) {
        if (categoriaId == null) return null;
        Categoria categoria = categorias.findByIdAndNegocio_Id(categoriaId, NegocioContext.getRequired())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoría indicada."));
        if (!categoria.isActiva()) throw new IllegalArgumentException("No se puede asociar un producto a una categoría inactiva.");
        return categoria;
    }

    private boolean bool(Boolean value, boolean defaultValue) { return value == null ? defaultValue : value; }

    private ProductoResponse toResponse(Producto producto) {
        List<ProductoImagen> imagenesProducto =
                imagenes.findByProducto_IdAndNegocio_IdOrderByOrdenAsc(producto.getId(), NegocioContext.getRequired());

        return ProductoResponse.from(producto, imagenesProducto, supabaseUrl);
    }
}
