package com.sistema.gestao.socios.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider; // Import ObjectProvider
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Keep this import
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Lombok for constructor injection
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    // Use ObjectProvider for lazy resolution
    private final ObjectProvider<UserDetailsService> userDetailsServiceProvideer; 

    // Keep @RequiredArgsConstructor, it will handle the ObjectProvider injection

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Continue chain if no token
            return;
        }

        // 2. Extract the token (substring after "Bearer ")
        jwt = authHeader.substring(7);

        // 3. Extract user email (subject) from the token using JwtService
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Handle potential exceptions during token parsing (e.g., expired, malformed)
            // Log the error or send an appropriate response if needed
            logger.warn("Error parsing JWT token: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }


        // 4. Check if email is extracted and user is not already authenticated
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 5. Get UserDetailsService instance from the provider
            UserDetailsService userDetailsService = this.userDetailsServiceProvideer.getObject(); 
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate the token against UserDetails
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 7. Create an authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are null for JWT-based auth
                        userDetails.getAuthorities()
                );
                // 8. Set details from the request
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 9. Update the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 10. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
