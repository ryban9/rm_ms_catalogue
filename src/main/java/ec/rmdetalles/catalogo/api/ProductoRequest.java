package ec.rmdetalles.catalogo.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductoRequest(
        UUID categoriaId,
        @NotBlank @Size(max = 50) String codigo,
        @NotBlank @Size(max = 160) String nombre,
        @Size(max = 5000) String descripcion,
        @PositiveOrZero BigDecimal precioReferencial,
        Boolean publicado,
        Boolean personalizable,
        Boolean controlaStock,
        Boolean activo
) { }
