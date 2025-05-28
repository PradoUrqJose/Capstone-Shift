package com.sportcenter.shift_manager.config;

import com.sportcenter.shift_manager.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Manejar solicitudes OPTIONS (preflight) directamente
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("📢 Procesando solicitud OPTIONS para: " + request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        System.out.println("🔍 Procesando solicitud: " + path);
        if (path.startsWith("/api/auth/") || path.startsWith("/swagger-ui/") || path.startsWith("/api-docs/")) {
            System.out.println("✅ Ruta pública, pasando sin autenticación");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("🔑 Token recibido: " + token);
            try {
                username = jwtUtil.extractUsername(token);
                System.out.println("👤 Username extraído: " + username);
            } catch (Exception e) {
                System.out.println("❌ Error al extraer username del token: " + e.getMessage());
            }
        } else {
            System.out.println("🚫 Sin cabecera Authorization o formato inválido");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = usuarioService.loadUserByUsername(username);
                System.out.println("✅ UserDetails cargado: " + userDetails.getUsername() + ", roles: " + userDetails.getAuthorities());
                if (jwtUtil.validateToken(token)) {
                    System.out.println("✅ Token válido, autenticando usuario");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("❌ Token inválido");
                }
            } catch (Exception e) {
                System.out.println("❌ Error al cargar UserDetails o validar token: " + e.getMessage());
            }
        } else if (username == null) {
            System.out.println("🚫 No se pudo extraer username del token");
        }

        filterChain.doFilter(request, response);
    }
}