package com.mangaon.recycleers.security;

import com.mangaon.recycleers.service.CustomUserDetailsService;
import com.mangaon.recycleers.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * ✅ FIX: The old version skipped JWT for ALL /api/** routes.
     *
     * That caused a conflict:
     *   - JWT filter skipped → no Authentication set in SecurityContext
     *   - SecurityConfig says /api/** requires authentication
     *   - Result: Spring Security returns 403 Forbidden
     *
     * Now we only skip the filter for truly public routes:
     *   - Static files (css, js, images)
     *   - HTML pages
     *   - /api/auth/** (login, register)
     *   - OPTIONS preflight requests (CORS)
     *
     * All other /api/** routes go through JWT validation.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path   = request.getServletPath();
        String method = request.getMethod();

        // ✅ Always skip OPTIONS preflight — CORS handles these
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // ✅ Skip for public auth endpoints (login, register)
        if (path.startsWith("/api/auth/")) {
            return true;
        }

        // ✅ Skip for static assets
        if (path.endsWith(".html") ||
                path.endsWith(".css")  ||
                path.endsWith(".js")   ||
                path.endsWith(".png")  ||
                path.endsWith(".jpg")  ||
                path.endsWith(".jpeg") ||
                path.endsWith(".ico")  ||
                path.endsWith(".svg")  ||
                path.endsWith(".webp")) {
            return true;
        }

        // ✅ Skip for static asset folders
        if (path.equals("/") ||
                path.startsWith("/css/")     ||
                path.startsWith("/js/")      ||
                path.startsWith("/images/")  ||
                path.startsWith("/fonts/")   ||
                path.startsWith("/assets/")  ||
                path.startsWith("/static/")  ||
                path.startsWith("/public/")  ||
                path.startsWith("/uploads/") ||
                path.startsWith("/webjars/")) {
            return true;
        }

        // ✅ Skip for Swagger (optional)
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui")) {
            return true;
        }

        // ❌ DO NOT skip /api/** — let the filter validate the JWT token.
        //    If no/invalid token → SecurityContext stays empty →
        //    SecurityConfig will return 401 (not 403) for protected routes.
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No token provided → continue without setting authentication.
        // SecurityConfig will decide if the route needs auth (401) or not.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt      = authHeader.substring(7);
        String username = null;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception ex) {
            // Malformed / expired token → continue without auth
            filterChain.doFilter(request, response);
            return;
        }

        // Token is structurally valid and no existing auth in context
        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ✅ Set authentication — SecurityConfig will now allow access
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}