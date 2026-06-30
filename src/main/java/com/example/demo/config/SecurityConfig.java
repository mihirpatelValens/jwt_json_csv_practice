package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.filter.JWTAuthenticationFilter;
import com.example.demo.filter.JWTValidationFilter;
import com.example.demo.utils.JWTUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  public SecurityConfig(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                 AuthenticationConfiguration config) throws Exception {

    AuthenticationManager authManager = authenticationManager(config);

    JWTAuthenticationFilter authenticationFilter =
            new JWTAuthenticationFilter(authManager, jwtUtil);

    JWTValidationFilter validationFilter =
            new JWTValidationFilter(jwtUtil, userDetailsService);

    System.out.println("Security Filter Chain");
    http
            .authorizeHttpRequests(auth -> auth
                    // Public endpoints — no token needed
                    .requestMatchers(
                            "/api/generate-token",
                            "/api/user-register",
                            "/api/find",
                            "/h2-console/**",
                            "/api/file/json/upload"
                    ).permitAll()
                    // Everything else requires a valid JWT
                    .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers(
                            "/api/generate-token",
                            "/api/user-register",
                            "/api/find",
                            "/h2-console/**",
                            "/api/file/json/upload"
                    )
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.disable()) // needed for H2 console
            )
            .authenticationProvider(daoAuthenticationProvider())
            // 1. JWTAuthenticationFilter — handles login, issues token
            // 2. JWTValidationFilter — validates token on protected routes
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(validationFilter, JWTAuthenticationFilter.class);

    return http.build();
  }
}