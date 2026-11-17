package ec.rmdetalles.catalogo.config;

import ec.rmdetalles.catalogo.repository.AccesoNegocioProjection;
import ec.rmdetalles.catalogo.repository.AccesoNegocioRepository;
import ec.rmdetalles.catalogo.repository.FrontendNegocioRepository;
import ec.rmdetalles.catalogo.repository.NegocioRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class NegocioAuthorizationFilter extends OncePerRequestFilter {

    private final AccesoNegocioRepository accesos;
    private final NegocioRepository negocios;
    private final FrontendNegocioRepository frontends;

    public NegocioAuthorizationFilter(
            AccesoNegocioRepository accesos,
            NegocioRepository negocios,
            FrontendNegocioRepository frontends
    ) {
        this.accesos = accesos;
        this.negocios = negocios;
        this.frontends = frontends;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/actuator/health") || "OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String negocioCodigo = request.getHeader("X-Negocio-Codigo");
        if (negocioCodigo == null || negocioCodigo.isBlank()) {
            responderError(response, 400, "Falta el encabezado X-Negocio-Codigo.");
            return;
        }

        negocioCodigo = negocioCodigo.trim().toUpperCase(Locale.ROOT);

        var negocio = negocios.findByCodigoAndActivoTrue(negocioCodigo);
        if (negocio.isEmpty()) {
            responderError(response, 404, "No se encontró un negocio activo con ese código.");
            return;
        }

        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()
                && !frontends.existsByNegocio_CodigoAndOrigenAndActivoTrue(negocioCodigo, origin)) {
            responderError(response, 403, "Este origen no está habilitado para este negocio.");
            return;
        }

        NegocioContext.set(negocio.get().getId());

        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean catalogoPublico = "GET".equals(request.getMethod())
                    && path.startsWith("/api/v1/catalogo/");

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String subject = jwtAuth.getToken().getSubject();
                if (subject == null) {
                    responderError(response, 401, "El identificador del usuario no es válido.");
                    return;
                }

                UUID usuarioId;
                try {
                    usuarioId = UUID.fromString(subject);
                } catch (IllegalArgumentException ex) {
                    responderError(response, 401, "El identificador del usuario no es válido.");
                    return;
                }

                List<AccesoNegocioProjection> filas =
                        accesos.buscarAcceso(usuarioId, negocioCodigo);

                if (!catalogoPublico && filas.isEmpty()) {
                    responderError(response, 403, "El usuario no tiene acceso a este negocio.");
                    return;
                }

                Set<GrantedAuthority> authorities = new LinkedHashSet<>();
                for (AccesoNegocioProjection fila : filas) {
                    if (fila.getRolCodigo() != null) {
                        authorities.add(new SimpleGrantedAuthority(
                                "ROLE_" + fila.getRolCodigo()
                        ));
                    }
                    if (fila.getPermisoCodigo() != null) {
                        authorities.add(new SimpleGrantedAuthority(
                                fila.getPermisoCodigo()
                        ));
                    }
                }

                var authenticationActualizado = new JwtAuthenticationToken(
                        jwtAuth.getToken(),
                        authorities,
                        jwtAuth.getName()
                );
                SecurityContextHolder.getContext()
                        .setAuthentication(authenticationActualizado);
            }

            filterChain.doFilter(request, response);
        } finally {
            NegocioContext.clear();
        }
    }

    private void responderError(
            HttpServletResponse response,
            int status,
            String mensaje
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"status\":" + status + ",\"detail\":\"" + mensaje + "\"}"
        );
    }
}
