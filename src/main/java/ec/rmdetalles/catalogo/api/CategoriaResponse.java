package ec.rmdetalles.catalogo.api;

import ec.rmdetalles.catalogo.domain.Categoria;

import java.util.UUID;

public record CategoriaResponse(UUID id, String nombre, String descripcion, boolean activa) {
    public static CategoriaResponse from(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion(), categoria.isActiva());
    }
}
