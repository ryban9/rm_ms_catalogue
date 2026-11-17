package ec.rmdetalles.catalogo.repository;

import java.util.UUID;

public interface AccesoNegocioProjection {
    UUID getNegocioId();
    String getRolCodigo();
    String getPermisoCodigo();
}
