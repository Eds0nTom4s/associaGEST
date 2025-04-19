package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RegisterRequestDTO;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.model.Role;
import com.sistema.gestao.socios.model.Usuario;
import com.sistema.gestao.socios.repository.UsuarioRepository;
import com.sistema.gestao.socios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Import specific exception
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Initialize mocks
class AuthenticationServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks // Inject mocks into this instance
    private AuthenticationService authenticationService;

    private RegisterRequestDTO registerRequestAdmin;
    private RegisterRequestDTO registerRequestSocio;
    private LoginRequestDTO loginRequest;
    private Usuario usuarioAdmin;
    private Usuario usuarioSocio;
    private final String testToken = "test-jwt-token";
    private final String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
        // Use builder pattern for RegisterRequestDTO
        registerRequestAdmin = RegisterRequestDTO.builder()
                .email("admin@test.com")
                .senha("password123")
                .role(Role.ADMIN)
                .build();
        registerRequestSocio = RegisterRequestDTO.builder()
                .email("socio@test.com")
                .senha("password456")
                .role(Role.SOCIO)
                .build();
        loginRequest = new LoginRequestDTO("admin@test.com", "password123");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setEmail(registerRequestAdmin.getEmail());
        usuarioAdmin.setSenha(encodedPassword); // Use encoded password
        usuarioAdmin.setRole(registerRequestAdmin.getRole());

        usuarioSocio = new Usuario();
        usuarioSocio.setId(2L);
        usuarioSocio.setEmail(registerRequestSocio.getEmail());
        usuarioSocio.setSenha(encodedPassword); // Use encoded password
        usuarioSocio.setRole(registerRequestSocio.getRole());
    }

    // --- Testes para register() ---

    @Test
    void register_shouldRegisterNewUserAndReturnToken_whenEmailNotExists() {
        // Arrange
        when(usuarioRepository.findByEmail(registerRequestAdmin.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequestAdmin.getSenha())).thenReturn(encodedPassword);
        // Use lenient() because save might not be called if other steps fail, preventing UnnecessaryStubbingException
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioAdmin); // Return saved user
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(testToken);

        // Act
        AuthenticationResponseDTO response = authenticationService.register(registerRequestAdmin);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        verify(usuarioRepository, times(1)).findByEmail(registerRequestAdmin.getEmail());
        verify(passwordEncoder, times(1)).encode(registerRequestAdmin.getSenha());
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); // Verify save was called
        verify(jwtService, times(1)).generateToken(any(Usuario.class));
    }

    @Test
    void register_shouldThrowRegraNegocioException_whenEmailAlreadyExists() {
        // Arrange
        when(usuarioRepository.findByEmail(registerRequestAdmin.getEmail())).thenReturn(Optional.of(usuarioAdmin));

        // Act & Assert
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            authenticationService.register(registerRequestAdmin);
        });

        assertEquals("Usuário com este email já existe: " + registerRequestAdmin.getEmail(), exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail(registerRequestAdmin.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }

    // --- Testes para login() ---

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // Arrange
        // Simulate successful authentication by AuthenticationManager (no exception thrown)
        // authenticationManager.authenticate(...) doesn't return anything on success
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuarioAdmin));
        when(jwtService.generateToken(usuarioAdmin)).thenReturn(testToken);

        // Act
        AuthenticationResponseDTO response = authenticationService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        // Verify authenticate was called with correct credentials
        verify(authenticationManager, times(1)).authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha())
        );
        verify(usuarioRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtService, times(1)).generateToken(usuarioAdmin);
    }

    @Test
    void login_shouldThrowRegraNegocioException_whenCredentialsAreInvalid() {
        // Arrange
        // Simulate failed authentication by AuthenticationManager
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials")); // Simulate Spring Security exception

        // Act & Assert
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Credenciais inválidas para o email: " + loginRequest.getEmail(), exception.getMessage());
        assertTrue(exception.getCause() instanceof BadCredentialsException); // Check underlying cause
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, never()).findByEmail(anyString()); // Should not be called if auth fails
        verify(jwtService, never()).generateToken(any(Usuario.class)); // Should not be called if auth fails
    }

     @Test
    void login_shouldThrowRegraNegocioException_whenUserNotFoundAfterSuccessfulAuth() {
        // Arrange - This case should ideally not happen if authenticate() passes, but test defensively
        // Simulate successful authentication
        // authenticationManager.authenticate(...) doesn't return anything on success

        // Simulate user mysteriously disappearing after successful authentication
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            authenticationService.login(loginRequest);
        });

        assertEquals("Erro inesperado: Usuário não encontrado após autenticação bem-sucedida.", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(jwtService, never()).generateToken(any(Usuario.class));
    }
}
