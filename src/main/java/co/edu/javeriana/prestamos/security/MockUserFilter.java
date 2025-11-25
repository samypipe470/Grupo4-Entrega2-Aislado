package co.edu.javeriana.prestamos.security;

import co.edu.javeriana.prestamos.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro para pruebas de API en aislamiento:
 * - Cuando está habilitado, si no hay JWT válido, inyecta un CustomUserDetails simulado
 *   usando los headers opcionales: X-User-Id y X-User-Roles (ESTUDIANTE|BIBLIOTECARIO|ADMIN)
 * - Permite que Postman ejerza diferentes roles sin depender de G2.
 */
@Component
public class MockUserFilter extends OncePerRequestFilter {

    private final boolean enabled;

    public MockUserFilter(@Value("${security.mock-user.enabled:true}") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication current = SecurityContextHolder.getContext().getAuthentication();
            boolean hasCustom = (current != null && current.getPrincipal() instanceof CustomUserDetails);

            if (enabled && !hasCustom) {
                // Permite sobreescribir por header; defaults sensatos
                String idHeader = request.getHeader("X-User-Id");
                Integer userId = null;
                try { userId = idHeader != null ? Integer.parseInt(idHeader) : 7; } catch (NumberFormatException ignored) { userId = 7; }

                String roles = String.valueOf(request.getHeader("X-User-Roles")).toUpperCase();
                int tipo = 1; // ESTUDIANTE por defecto
                if (roles.contains("ADMIN")) tipo = 3; else if (roles.contains("BIBLIOTECARIO")) tipo = 2;

                Usuario u = new Usuario(userId, "mock-" + userId, "Mock User", tipo);
                CustomUserDetails cud = new CustomUserDetails(u);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception ignored) {
        }

        filterChain.doFilter(request, response);
    }
}

