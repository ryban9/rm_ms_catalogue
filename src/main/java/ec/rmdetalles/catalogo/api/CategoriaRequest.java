package ec.rmdetalles.catalogo.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 2000) String descripcion,
        Boolean activa
) { }
