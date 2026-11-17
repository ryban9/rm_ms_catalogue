package ec.rmdetalles.catalogo.config;

import java.util.UUID;

public final class NegocioContext {
    private static final ThreadLocal<UUID> NEGOCIO_ACTUAL = new ThreadLocal<>();

    private NegocioContext() { }

    public static void set(UUID negocioId) {
        NEGOCIO_ACTUAL.set(negocioId);
    }

    public static UUID getRequired() {
        UUID negocioId = NEGOCIO_ACTUAL.get();
        if (negocioId == null) {
            throw new IllegalStateException(
                    "La solicitud no tiene un negocio validado."
            );
        }
        return negocioId;
    }

    public static void clear() {
        NEGOCIO_ACTUAL.remove();
    }
}
