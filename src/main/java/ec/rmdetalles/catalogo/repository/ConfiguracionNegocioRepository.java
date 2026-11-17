package ec.rmdetalles.catalogo.repository;

import ec.rmdetalles.catalogo.domain.Negocio;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfiguracionNegocioRepository
        extends Repository<Negocio, UUID> {

    @Query(value = """
        SELECT valor #>> '{}'
        FROM rm_detalles.configuraciones_negocio
        WHERE negocio_id = :negocioId
          AND clave = :clave
        """, nativeQuery = true)
    Optional<String> buscarValor(
            @Param("negocioId") UUID negocioId,
            @Param("clave") String clave
    );

    @Query(value = """
        SELECT jsonb_array_elements_text(c.valor)
        FROM rm_detalles.configuraciones_negocio c
        WHERE c.negocio_id = :negocioId
          AND c.clave = :clave
        """, nativeQuery = true)
    List<String> buscarLista(
            @Param("negocioId") UUID negocioId,
            @Param("clave") String clave
    );
}
