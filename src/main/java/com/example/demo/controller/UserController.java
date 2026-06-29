package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.UserRegistorEntity;
import com.example.demo.service.UserRegistorEntityService;

@RestController
@RequestMapping("api")
public class UserController {

    @Autowired
    private UserRegistorEntityService userRegistorEntityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Public Endpoints ──────────────────────────────────────────────────────

    @GetMapping("/find")
    public ResponseEntity<String> findUser(@RequestParam String param) {
        return ResponseEntity.ok("Hello, param received: " + param);
    }

    @PostMapping("/user-register")
    public ResponseEntity<String> register(@RequestBody UserRegistorEntity userRegistorDetails) {
        userRegistorDetails.setPassword(passwordEncoder.encode(userRegistorDetails.getPassword()));
        userRegistorEntityService.save(userRegistorDetails);
        return ResponseEntity.ok("User registered successfully");
    }

    /**
     * This endpoint is handled entirely by JWTAuthenticationFilter.
     * The filter intercepts POST /api/generate-token, authenticates credentials,
     * and writes the token JSON response directly — this method never executes.
     *
     * It must exist so Spring registers the route and permitAll() applies to it.
     */
    @PostMapping("/generate-token")
    public ResponseEntity<LoginResponseDTO> generateToken(@RequestBody LoginRequestDTO loginRequest) {
        // Handled by JWTAuthenticationFilter — this body is never reached
        return ResponseEntity.ok(new LoginResponseDTO(null, null, "Handled by filter"));
    }

    // ── Protected Endpoints ───────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/users/me")
    public ResponseEntity<LoginResponseDTO> getMyDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(
                    new LoginResponseDTO(null, null, "Not authenticated")
            );
        }

        return ResponseEntity.ok(
                new LoginResponseDTO(null, authentication.getName(), "Fetched user details successfully")
        );
    }

    @GetMapping("/users")
    public ResponseEntity<String> getUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Authenticated as: " + authentication.getName());
    }
}