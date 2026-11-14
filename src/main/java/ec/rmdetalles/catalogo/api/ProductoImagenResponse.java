package ec.rmdetalles.catalogo.api;

import ec.rmdetalles.catalogo.domain.ProductoImagen;

import java.util.UUID;

public record ProductoImagenResponse(
        UUID id,
        String rutaStorage,
        String url,
        String textoAlternativo,
        short orden,
        boolean principal
) {
    public static ProductoImagenResponse from(ProductoImagen imagen, String supabaseUrl) {
        String rutaCodificada = imagen.getRutaStorage().replace(" ", "%20");
        String url = supabaseUrl.replaceAll("/+$", "")
                + "/storage/v1/object/public/productos/"
                + rutaCodificada;

        return new ProductoImagenResponse(
                imagen.getId(),
                imagen.getRutaStorage(),
                url,
                imagen.getTextoAlternativo(),
                imagen.getOrden(),
                imagen.isPrincipal()
        );
    }
}
