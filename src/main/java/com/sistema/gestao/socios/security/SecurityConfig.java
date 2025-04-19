package com.sistema.gestao.socios.security;

// Removed UsuarioRepository import
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Lazy; // No longer needed here
// Removed AuthenticationManager import (defined in ApplicationConfig)
import org.springframework.security.authentication.AuthenticationProvider;
// Removed DaoAuthenticationProvider import (defined in ApplicationConfig)
// Removed AuthenticationConfiguration import (defined in ApplicationConfig)
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
// Removed UserDetailsService import (defined in ApplicationConfig)
// Removed UsernameNotFoundException import (defined in ApplicationConfig)
// Removed BCryptPasswordEncoder import (defined in ApplicationConfig)
// Removed PasswordEncoder import (defined in ApplicationConfig)
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Import the filter
import org.springframework.security.web.AuthenticationEntryPoint; // Import AuthenticationEntryPoint
import org.springframework.security.core.AuthenticationException; // Import AuthenticationException
import jakarta.servlet.http.HttpServletResponse; // Import HttpServletResponse
import java.io.IOException; // Import IOException


@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable method-level security annotations
@RequiredArgsConstructor // Lombok will generate the constructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider; // Keep this, it's provided by ApplicationConfig

    // Define a custom AuthenticationEntryPoint for handling 401 Unauthorized errors
    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            // You can log the error here if needed:
            // log.error("Unauthorized error: {}", authException.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Acesso não autorizado"); // Send 401
        };
    }

    // Define public paths for better readability and management
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",          // Authentication endpoints
            "/api-docs/**",         // OpenAPI spec JSON/YAML (Corrected path based on application.properties)
            "/swagger-ui/**",       // Swagger UI resources (CSS, JS, etc.)
            "/swagger-ui.html"      // Swagger UI main page
            // Add other public paths here if needed, e.g., "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless API
                // Configure exception handling to use the custom entry point for 401 errors
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint())
                )
                .authorizeHttpRequests(auth -> auth
                        // Permit access to public paths defined above
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Admin-specific endpoints (ensure these come AFTER permitAll)
                        .requestMatchers("/api/administradores/**", "/api/categorias/**", "/api/relatorios-financeiros/**").hasRole("ADMIN")
                        // Socio and Admin endpoints (ensure these come AFTER permitAll)
                        .requestMatchers("/api/socios/**", "/api/pagamentos/**", "/api/notificacoes/**").hasAnyRole("ADMIN", "SOCIO")
                        // Secure any other request (require authentication)
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions
                )
                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider) // Set the custom auth provider
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add the filter

        return http.build();
    }

    // Removed userDetailsService bean (moved to ApplicationConfig)

    // Removed authenticationManager bean (moved to ApplicationConfig)

    // Removed passwordEncoder bean (moved to ApplicationConfig)
}
