package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RegisterRequestDTO;
import com.sistema.gestao.socios.model.Role;
import com.sistema.gestao.socios.model.Usuario;
import com.sistema.gestao.socios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Carrega o contexto completo da aplicação
@AutoConfigureMockMvc // Configura o MockMvc
@Transactional // Garante que cada teste rode em uma transação que será revertida
class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos Java para JSON e vice-versa

    @Autowired
    private UsuarioRepository usuarioRepository; // Para interagir com o banco de dados

    @Autowired
    private PasswordEncoder passwordEncoder; // Para codificar senhas ao preparar dados

    private RegisterRequestDTO registerAdminDTO;
    private RegisterRequestDTO registerSocioDTO;
    private LoginRequestDTO loginAdminDTO;
    private LoginRequestDTO loginSocioDTO;

    @BeforeEach
    void setUp() {
        // Limpa o repositório antes de cada teste para garantir isolamento
        // @Transactional já faz rollback, mas limpar explicitamente pode evitar side-effects entre testes se @Transactional for removido
        usuarioRepository.deleteAll();

        // DTOs para registro
        registerAdminDTO = RegisterRequestDTO.builder()
                .email("admin.integration@test.com")
                .senha("password123")
                .role(Role.ADMIN)
                .build();

        registerSocioDTO = RegisterRequestDTO.builder()
                .email("socio.integration@test.com")
                .senha("password456")
                .role(Role.SOCIO)
                .build();

        // DTOs para login (correspondentes aos de registro)
        loginAdminDTO = new LoginRequestDTO(registerAdminDTO.getEmail(), registerAdminDTO.getSenha());
        loginSocioDTO = new LoginRequestDTO(registerSocioDTO.getEmail(), registerSocioDTO.getSenha());
    }

    // --- Testes para POST /api/auth/register ---

    @Test
    @DisplayName("Deve registrar um novo usuário ADMIN com sucesso e retornar token")
    void register_shouldRegisterAdminUserAndReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerAdminDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue())); // Verifica se o token não é nulo

        // Verifica se o usuário foi realmente salvo no banco
        assertTrue(usuarioRepository.findByEmail(registerAdminDTO.getEmail()).isPresent());
    }

    @Test
    @DisplayName("Deve registrar um novo usuário SOCIO com sucesso e retornar token")
    void register_shouldRegisterSocioUserAndReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerSocioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        assertTrue(usuarioRepository.findByEmail(registerSocioDTO.getEmail()).isPresent());
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar registrar usuário com email existente")
    void register_shouldReturnBadRequestWhenEmailExists() throws Exception {
        // Primeiro, registra um usuário
        Usuario existingUser = new Usuario();
        existingUser.setEmail(registerAdminDTO.getEmail());
        existingUser.setSenha(passwordEncoder.encode(registerAdminDTO.getSenha()));
        existingUser.setRole(registerAdminDTO.getRole());
        usuarioRepository.save(existingUser);

        // Tenta registrar novamente com o mesmo email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerAdminDTO)))
                .andExpect(status().isBadRequest()) // Espera Bad Request (RegraNegocioException é mapeada para 400 pelo GlobalExceptionHandler)
                .andExpect(jsonPath("$.message", is("Usuário com este email já existe: " + registerAdminDTO.getEmail())));
    }

    // --- Testes para POST /api/auth/login ---

    @Test
    @DisplayName("Deve autenticar usuário ADMIN com credenciais válidas e retornar token")
    void login_shouldAuthenticateAdminAndReturnTokenWithValidCredentials() throws Exception {
        // Prepara: Salva o usuário ADMIN no banco
        Usuario adminUser = new Usuario();
        adminUser.setEmail(registerAdminDTO.getEmail());
        adminUser.setSenha(passwordEncoder.encode(registerAdminDTO.getSenha())); // Salva senha codificada
        adminUser.setRole(registerAdminDTO.getRole());
        usuarioRepository.save(adminUser);

        // Executa: Tenta fazer login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAdminDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

     @Test
    @DisplayName("Deve autenticar usuário SOCIO com credenciais válidas e retornar token")
    void login_shouldAuthenticateSocioAndReturnTokenWithValidCredentials() throws Exception {
        // Prepara: Salva o usuário SOCIO no banco
        Usuario socioUser = new Usuario();
        socioUser.setEmail(registerSocioDTO.getEmail());
        socioUser.setSenha(passwordEncoder.encode(registerSocioDTO.getSenha())); // Salva senha codificada
        socioUser.setRole(registerSocioDTO.getRole());
        usuarioRepository.save(socioUser);

        // Executa: Tenta fazer login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginSocioDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }


    @Test
    @DisplayName("Deve retornar erro 400 ao tentar autenticar com senha inválida")
    void login_shouldReturnBadRequestWithInvalidPassword() throws Exception {
        // Prepara: Salva o usuário ADMIN no banco
        Usuario adminUser = new Usuario();
        adminUser.setEmail(registerAdminDTO.getEmail());
        adminUser.setSenha(passwordEncoder.encode(registerAdminDTO.getSenha()));
        adminUser.setRole(registerAdminDTO.getRole());
        usuarioRepository.save(adminUser);

        // Cria DTO de login com senha errada
        LoginRequestDTO invalidLoginDTO = new LoginRequestDTO(loginAdminDTO.getEmail(), "wrongpassword");

        // Executa: Tenta fazer login com senha errada
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginDTO)))
                .andExpect(status().isBadRequest()) // Espera Bad Request (RegraNegocioException)
                .andExpect(jsonPath("$.message", is("Credenciais inválidas para o email: " + loginAdminDTO.getEmail())));
    }

    @Test
    @DisplayName("Deve retornar erro 400 ao tentar autenticar com email inexistente")
    void login_shouldReturnBadRequestWithNonExistentEmail() throws Exception {
        // Cria DTO de login com email que não existe
        LoginRequestDTO nonExistentLoginDTO = new LoginRequestDTO("nonexistent@test.com", "password123");

        // Executa: Tenta fazer login com email inexistente
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistentLoginDTO)))
                .andExpect(status().isBadRequest()) // Espera Bad Request (RegraNegocioException)
                 .andExpect(jsonPath("$.message", is("Credenciais inválidas para o email: " + nonExistentLoginDTO.getEmail())));
    }

    // --- Testes de Acesso a Rotas Protegidas ---

    // Helper method to perform login and get token
    private String loginAndGetToken(LoginRequestDTO loginDTO) throws Exception {
         MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponseDTO authResponse = objectMapper.readValue(responseBody, AuthenticationResponseDTO.class);
        return authResponse.getToken();
    }

    @Test
    @DisplayName("Deve retornar 401 ao acessar rota protegida sem token")
    void protectedRoute_shouldReturnUnauthorizedWithoutToken() throws Exception {
        // Tenta acessar /api/socios (requer ADMIN ou SOCIO) sem token
        mockMvc.perform(get("/api/socios") // Exemplo de rota protegida
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Ou isForbidden() dependendo da config exata
    }

    @Test
    @DisplayName("Deve permitir acesso a rota protegida (SOCIO) com token de ADMIN válido")
    void protectedRoute_shouldAllowAccessForAdminWithValidToken() throws Exception {
        // Prepara: Registra e loga ADMIN
        Usuario adminUser = new Usuario();
        adminUser.setEmail(registerAdminDTO.getEmail());
        adminUser.setSenha(passwordEncoder.encode(registerAdminDTO.getSenha()));
        adminUser.setRole(registerAdminDTO.getRole());
        usuarioRepository.save(adminUser);
        String adminToken = loginAndGetToken(loginAdminDTO);

        // Executa: Acessa rota de SOCIO/ADMIN com token de ADMIN
        mockMvc.perform(get("/api/socios") // Rota acessível por ADMIN e SOCIO
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Espera sucesso (ou 404 se /api/socios sem ID não for mapeado)
    }

     @Test
    @DisplayName("Deve permitir acesso a rota protegida (SOCIO) com token de SOCIO válido")
    void protectedRoute_shouldAllowAccessForSocioWithValidToken() throws Exception {
        // Prepara: Registra e loga SOCIO
        Usuario socioUser = new Usuario();
        socioUser.setEmail(registerSocioDTO.getEmail());
        socioUser.setSenha(passwordEncoder.encode(registerSocioDTO.getSenha()));
        socioUser.setRole(registerSocioDTO.getRole());
        usuarioRepository.save(socioUser);
        String socioToken = loginAndGetToken(loginSocioDTO);

        // Executa: Acessa rota de SOCIO/ADMIN com token de SOCIO
        mockMvc.perform(get("/api/socios") // Rota acessível por ADMIN e SOCIO
                        .header("Authorization", "Bearer " + socioToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Espera sucesso
    }


    @Test
    @DisplayName("Deve retornar 403 ao acessar rota ADMIN com token de SOCIO")
    void protectedRoute_shouldReturnForbiddenForSocioOnAdminRoute() throws Exception {
         // Prepara: Registra e loga SOCIO
        Usuario socioUser = new Usuario();
        socioUser.setEmail(registerSocioDTO.getEmail());
        socioUser.setSenha(passwordEncoder.encode(registerSocioDTO.getSenha()));
        socioUser.setRole(registerSocioDTO.getRole());
        usuarioRepository.save(socioUser);
        String socioToken = loginAndGetToken(loginSocioDTO);

        // Executa: Tenta acessar rota exclusiva de ADMIN (/api/categorias) com token de SOCIO
        mockMvc.perform(get("/api/categorias") // Rota apenas ADMIN
                        .header("Authorization", "Bearer " + socioToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Espera Forbidden
    }

    @Test
    @DisplayName("Deve retornar 401 ao acessar rota protegida com token inválido/malformado")
    void protectedRoute_shouldReturnUnauthorizedWithInvalidToken() throws Exception {
        String invalidToken = "this.is.not.valid.token";
        mockMvc.perform(get("/api/socios")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Ou isForbidden()
    }

     // Teste para token expirado é mais complexo de simular sem libs de manipulação de tempo
}
