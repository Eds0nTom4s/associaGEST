package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RegisterRequestDTO;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.model.Usuario;
import com.sistema.gestao.socios.repository.UsuarioRepository;
import com.sistema.gestao.socios.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDTO register(RegisterRequestDTO request) {
        // Check if user already exists
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegraNegocioException("Usuário com este email já existe: " + request.getEmail());
        }

        // Create new user entity
        var usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setSenha(passwordEncoder.encode(request.getSenha())); // Hash the password
        usuario.setRole(request.getRole()); // Assign role from request

        // Save the user
        usuarioRepository.save(usuario);

        // Generate JWT token for the new user
        var jwtToken = jwtService.generateToken(usuario);

        // Return the token
        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponseDTO login(LoginRequestDTO request) {
        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );
        } catch (AuthenticationException e) {
            // Handle incorrect credentials
            throw new RegraNegocioException("Credenciais inválidas para o email: " + request.getEmail(), e);
        }


        // If authentication is successful, find the user (should exist)
        var usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RegraNegocioException("Erro inesperado: Usuário não encontrado após autenticação bem-sucedida.")); // Should not happen if authenticate passed

        // Generate JWT token
        var jwtToken = jwtService.generateToken(usuario);

        // Return the token
        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}
