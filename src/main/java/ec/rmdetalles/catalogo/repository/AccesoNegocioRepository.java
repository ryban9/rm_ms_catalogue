package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccesoNegocioRepository extends Repository<Usuario, UUID> {

    @Query(value = """
        SELECT DISTINCT
            n.id AS "negocioId",
            r.codigo AS "rolCodigo",
            p.codigo AS "permisoCodigo"
        FROM rm_detalles.usuarios u
        JOIN rm_detalles.usuario_negocios un
            ON un.usuario_id = u.id
        JOIN rm_detalles.negocios n
            ON n.id = un.negocio_id
        JOIN rm_detalles.roles r
            ON r.id = un.rol_id
           AND r.negocio_id = un.negocio_id
        LEFT JOIN rm_detalles.rol_permisos rp
            ON rp.rol_id = r.id
           AND rp.concedido = TRUE
        LEFT JOIN rm_detalles.permisos p
            ON p.id = rp.permiso_id
        WHERE u.id = :usuarioId
          AND u.activo = TRUE
          AND un.activo = TRUE
          AND n.codigo = :negocioCodigo
          AND n.activo = TRUE
          AND r.activo = TRUE
        """, nativeQuery = true)
    List<AccesoNegocioProjection> buscarAcceso(
            @Param("usuarioId") UUID usuarioId,
            @Param("negocioCodigo") String negocioCodigo
    );
}
