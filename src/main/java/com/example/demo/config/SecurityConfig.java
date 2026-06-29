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
import com.example.demo.utils.JWTUtil;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private JWTUtil jwtUtil;
    private UserDetailsService userDetailsService;

    public SecurityConfig(JWTUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager,
            JWTUtil jwtUtil) {
        return new JWTAuthenticationFilter(authenticationManager, jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationConfiguration config)
            throws Exception {

        System.out.println("Coming to security filter chain");
        JWTAuthenticationFilter jwtAuthFilter = new JWTAuthenticationFilter(authenticationManager(config), jwtUtil);

        http.authorizeHttpRequests(
                auth -> auth.requestMatchers("/api/user-register", "/h2-console/**", "/find").permitAll().anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/user-register"))
                .headers(headers -> headers.frameOptions((frame -> frame.disable())));
                // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        // .csrf(csrf -> csrf.disable());

        http.addFilterBefore(jwtAuthenticationFilter(authenticationManager(config), jwtUtil),
                     UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // @Bean
    // public AuthenticationManager authenticationManager() {
    // return new ProviderManager(Arrays.asList(daoAuthenticationProvider()));
    // }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
