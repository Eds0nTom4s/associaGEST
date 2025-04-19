package com.sistema.gestao.socios.security;

import com.sistema.gestao.socios.model.Role;
import com.sistema.gestao.socios.model.Usuario;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils; // Para injetar valores de properties

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetailsAdmin;
    private UserDetails userDetailsSocio;
    private Usuario usuarioAdmin; // Usado para gerar token

    // Chave e expiração para teste (iguais às de application.properties para consistência, mas poderiam ser diferentes)
    private final String testSecretKey = "4D6251655468576D5A7134743777217A25432A462D4A614E645267556B587032";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injeta os valores da chave e expiração no serviço usando ReflectionTestUtils
        // Isso simula a injeção de @Value("${application.security.jwt.secret-key}") etc.
        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // Cria UserDetails para teste
        userDetailsAdmin = User.builder()
                .username("admin@test.com")
                .password("password")
                .roles(Role.ADMIN.name()) // Usa o nome do Enum
                .build();

        userDetailsSocio = User.builder()
                .username("socio@test.com")
                .password("password")
                .roles(Role.SOCIO.name())
                .build();

        // Cria um objeto Usuario correspondente para gerar o token inicial
        usuarioAdmin = new Usuario();
        usuarioAdmin.setEmail("admin@test.com");
        usuarioAdmin.setRole(Role.ADMIN); // Define o Role Enum
        // Não precisamos de senha aqui, pois UserDetails é usado para validação
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(usuarioAdmin);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(usuarioAdmin.getEmail(), extractedUsername);
    }

    @Test
    void generateToken_shouldGenerateValidTokenForUsuario() {
        String token = jwtService.generateToken(usuarioAdmin);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetailsAdmin));
        assertEquals(usuarioAdmin.getEmail(), jwtService.extractUsername(token));
    }

    @Test
    void generateTokenWithExtraClaims_shouldContainExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        String token = jwtService.generateToken(extraClaims, usuarioAdmin);
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token, userDetailsAdmin));
        assertEquals("customValue", jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class)));
    }


    @Test
    void isTokenValid_shouldReturnTrueForValidTokenAndMatchingUserDetails() {
        String token = jwtService.generateToken(usuarioAdmin);
        assertTrue(jwtService.isTokenValid(token, userDetailsAdmin));
    }

    @Test
    void isTokenValid_shouldReturnFalseForValidTokenAndNonMatchingUserDetails() {
        String token = jwtService.generateToken(usuarioAdmin); // Token para admin@test.com
        assertFalse(jwtService.isTokenValid(token, userDetailsSocio)); // Valida contra socio@test.com
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws InterruptedException {
        // Gera um token com expiração muito curta (1ms)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L);
        String expiredToken = jwtService.generateToken(usuarioAdmin);

        // Espera um pouco para garantir que o token expire
        Thread.sleep(50); // Espera 50ms

        // Restaura a expiração original para não afetar outros testes
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", testExpiration);

        // Tenta validar o token expirado
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenValid(expiredToken, userDetailsAdmin));
        // Ou, se quisermos apenas false sem exceção (depende da implementação exata de isTokenValid)
        // assertFalse(jwtService.isTokenValid(expiredToken, userDetailsAdmin)); // Ajustar se necessário
    }

     @Test
    void isTokenValid_shouldThrowMalformedJwtExceptionForInvalidToken() {
        String invalidToken = "this.is.not.a.valid.token";
        assertThrows(MalformedJwtException.class, () -> jwtService.isTokenValid(invalidToken, userDetailsAdmin));
    }

    @Test
    void extractExpiration_shouldReturnCorrectDate() {
        long now = System.currentTimeMillis();
        String token = jwtService.generateToken(usuarioAdmin);
        Date expirationDate = jwtService.extractExpiration(token);

        assertNotNull(expirationDate);
        // Verifica se a data de expiração está aproximadamente correta (dentro de uma margem)
        assertTrue(expirationDate.getTime() > now);
        assertTrue(expirationDate.getTime() <= now + testExpiration + 1000); // Adiciona 1s de margem
    }
}
