package ec.rmdetalles.catalogo.api;

import ec.rmdetalles.catalogo.domain.Producto;
import ec.rmdetalles.catalogo.domain.ProductoImagen;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductoResponse(
        UUID id,
        UUID categoriaId,
        String categoriaNombre,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precioReferencial,
        boolean publicado,
        boolean personalizable,
        boolean controlaStock,
        boolean activo,
        List<ProductoImagenResponse> imagenes
) {
    public static ProductoResponse from(Producto producto) {
        return from(producto, List.of(), "");
    }

    public static ProductoResponse from(
            Producto producto,
            List<ProductoImagen> imagenes,
            String supabaseUrl
    ) {
        UUID categoriaId = producto.getCategoria() == null
                ? null : producto.getCategoria().getId();
        String categoriaNombre = producto.getCategoria() == null
                ? null : producto.getCategoria().getNombre();

        List<ProductoImagenResponse> imagenesResponse = imagenes.stream()
                .map(imagen -> ProductoImagenResponse.from(imagen, supabaseUrl))
                .toList();

        return new ProductoResponse(
                producto.getId(),
                categoriaId,
                categoriaNombre,
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecioReferencial(),
                producto.isPublicado(),
                producto.isPersonalizable(),
                producto.isControlaStock(),
                producto.isActivo(),
                imagenesResponse
        );
    }
}
