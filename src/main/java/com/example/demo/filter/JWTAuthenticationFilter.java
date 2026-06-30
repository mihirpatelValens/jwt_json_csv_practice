package com.example.demo.filter;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;  // was: tools.jackson.databind.ObjectMapper

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("Come to Authentication Filter");

        // This filter only handles the login/token-generation endpoint
        if (!request.getRequestURI().equals("/api/generate-token")
                || !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWTAuthenticationFilter triggered for: " + request.getRequestURI());

        ObjectMapper objectMapper = new ObjectMapper();

        // Parse credentials from request body
        LoginRequestDTO loginRequest;
        try {
            loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request body");
            return;
        }

        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
            return;
        }

        try {
            // Authenticate credentials against DB
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    );

            Authentication authResult = authenticationManager.authenticate(authToken);

            if (authResult.isAuthenticated()) {
                String token = jwtUtil.generateToken(authResult.getName(), 15);

                // Return token as JSON body + Authorization header
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setHeader("Authorization", "Bearer " + token);
                response.getWriter().write(
                        "{\"token\": \"" + token + "\", \"username\": \"" + authResult.getName() + "\"}"
                );
            }

        } catch (BadCredentialsException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
        }

        // Do NOT call filterChain.doFilter() here — response is already written
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"error\": \"" + message + "\", \"statusCode\": " + statusCode + "}"
        );
    }
}