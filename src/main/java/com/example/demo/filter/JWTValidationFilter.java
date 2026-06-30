package com.example.demo.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.utils.JWTUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTValidationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JWTValidationFilter(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Validating JWT Token");
        String requestURI = request.getRequestURI();

        // Skip validation for public endpoints — let them pass straight through
        if (requestURI.equals("/api/generate-token")
                || requestURI.equals("/api/user-register")
                || requestURI.startsWith("/h2-console") || requestURI.startsWith("/api/json/upload")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // No token present — let Spring Security handle the 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip "Bearer "

        try {
            if (!jwtUtil.validateToken(token)) {
                sendUnauthorized(response, "Token is expired or invalid");
                return;
            }

            String username = jwtUtil.extractUsername(token);

            // Only set auth if not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Put authentication into the SecurityContext for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            System.out.println("JWT validation error: " + e.getMessage());
            sendUnauthorized(response, "Token validation failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"" + message + "\", \"statusCode\": 401}"
        );
    }
}