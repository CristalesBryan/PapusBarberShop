package com.papusbarbershop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro CORS con máxima prioridad para garantizar que las peticiones desde
 * gestion.papusbarbershop.com (y otros orígenes permitidos) reciban siempre
 * los headers Access-Control-Allow-Origin en la respuesta, incluyendo preflight OPTIONS.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "https://gestion.papusbarbershop.com",
            "https://www.papusbarbershop.com",
            "https://papusbarbershop.com",
            "http://localhost:4200",
            "http://localhost:3000",
            "http://localhost:5173"
    );

    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, PATCH";
    private static final String ALLOWED_HEADERS = "*";
    private static final String EXPOSED_HEADERS = "Authorization";
    private static final boolean ALLOW_CREDENTIALS = true;
    private static final long MAX_AGE = 3600L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String origin = request.getHeader("Origin");

        if (origin != null && isAllowedOrigin(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", String.valueOf(ALLOW_CREDENTIALS));
            response.setHeader("Access-Control-Allow-Methods", ALLOWED_METHODS);
            response.setHeader("Access-Control-Allow-Headers", ALLOWED_HEADERS);
            response.setHeader("Access-Control-Expose-Headers", EXPOSED_HEADERS);
            response.setHeader("Access-Control-Max-Age", String.valueOf(MAX_AGE));
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) return false;
        if (ALLOWED_ORIGINS.contains(origin)) return true;
        if (origin.matches("https://[a-zA-Z0-9-]+\\.up\\.railway\\.app")) return true;
        return false;
    }
}
