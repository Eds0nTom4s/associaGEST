package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RegisterRequestDTO;
import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Role;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.model.Usuario;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.PagamentoRepository;
import com.sistema.gestao.socios.repository.SocioRepository;
import com.sistema.gestao.socios.repository.UsuarioRepository;
import com.sistema.gestao.socios.security.JwtService; // Import JwtService
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Import UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
// MvcResult and AuthenticationResponseDTO no longer needed for token generation here
// import org.springframework.test.web.servlet.MvcResult;
// import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
// import com.sistema.gestao.socios.dto.LoginRequestDTO;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Use TestInstance.Lifecycle.PER_CLASS to generate token once for all tests
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SocioControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.socio@test.com";
    private static final String ADMIN_PASSWORD = "password";
    private String adminToken;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inject UsuarioRepository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService; // Inject JwtService

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService (provided by ApplicationConfig)

    // No need for AuthenticationService injection if we perform login via MockMvc

    private Categoria savedCategoria;
    private Socio socio1;
    private Socio socio2;
    private SocioRequestDTO socioRequestDTO;

    // Use @BeforeAll with PER_CLASS lifecycle to setup token once
    @BeforeAll
    void setupAuthentication() throws Exception {
        // Clean user repo before creating the admin user
        usuarioRepository.deleteAll();

        // Create Admin User for authentication
        // Removed .nome() as Usuario entity does not have a 'nome' field
        Usuario adminUser = Usuario.builder()
                .email(ADMIN_EMAIL)
                .senha(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();
        usuarioRepository.save(adminUser);

        // Generate token directly using JwtService
        UserDetails userDetails = userDetailsService.loadUserByUsername(ADMIN_EMAIL);
        adminToken = jwtService.generateToken(userDetails);
    }


    @BeforeEach
    void setupTestData() {
        // Clean data repositories before each test, but not the user repository
        pagamentoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Test data creation
        Categoria categoria = new Categoria(null, "Standard", "Básico", new BigDecimal("50.00"), null, null);
        savedCategoria = categoriaRepository.save(categoria);

        socio1 = new Socio(null, "Ana", "111", "ana@test.com", "123", "pass", "PENDENTE", savedCategoria, null, null);
        socio2 = new Socio(null, "Beto", "222", "beto@test.com", "456", "pass", "PAGO", savedCategoria, null, null);

        socioRequestDTO = new SocioRequestDTO();
        socioRequestDTO.setNome("Carlos");
        socioRequestDTO.setDocumento("333");
        socioRequestDTO.setEmail("carlos@test.com");
        socioRequestDTO.setTelefone("789");
        socioRequestDTO.setSenha("senha123");
        socioRequestDTO.setCategoriaId(savedCategoria.getId());
    }

    // --- Helper method to add Authorization header ---
    private String getAuthHeader() {
        return "Bearer " + adminToken;
    }

    @Test
    void testCadastrarSocio_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is(socioRequestDTO.getNome())))
                .andExpect(jsonPath("$.email", is(socioRequestDTO.getEmail())))
                .andExpect(jsonPath("$.documento", is(socioRequestDTO.getDocumento())))
                .andExpect(jsonPath("$.statusPagamento", is("PENDENTE"))) // Check initial status
                .andExpect(jsonPath("$.categoria.id", is(savedCategoria.getId().intValue())));
    }

    @Test
    void testCadastrarSocio_BadRequest_CategoriaNotFound() throws Exception {
        // given
        socioRequestDTO.setCategoriaId(999L); // Non-existent ID

        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound()) // Expect 404 as CategoriaService throws RecursoNaoEncontradoException
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: 999"))); // Expect lowercase 'id'
    }

     @Test
    void testCadastrarSocio_BadRequest_EmailExists() throws Exception {
        // given
        socioRepository.save(socio1); // Save Ana with email ana@test.com
        socioRequestDTO.setEmail("ana@test.com"); // Try to save Carlos with Ana's email

        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email já cadastrado: ana@test.com")));
    }

     @Test
    void testCadastrarSocio_BadRequest_DocumentoExists() throws Exception {
        // given
        socioRepository.save(socio1); // Save Ana with doc 111
        socioRequestDTO.setDocumento("111"); // Try to save Carlos with Ana's doc

        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Documento já cadastrado: 111")));
    }

     @Test
    void testCadastrarSocio_BadRequest_InvalidDTO() throws Exception {
        // given
        socioRequestDTO.setNome(""); // Invalid name

        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("nome"))) // Check validation error format
                .andExpect(jsonPath("$[0].message", is("Nome não pode ser vazio")));
    }


    @Test
    void testListarTodosSocios() throws Exception {
        // given
        List<Socio> socios = new ArrayList<>();
        socios.add(socio1);
        socios.add(socio2);
        socioRepository.saveAll(socios);

        // when
        ResultActions response = mockMvc.perform(get("/api/socios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(socios.size())))
                .andExpect(jsonPath("$[0].nome", is(socio1.getNome())))
                .andExpect(jsonPath("$[1].nome", is(socio2.getNome())));
    }

    @Test
    void testBuscarSocioPorId_Success() throws Exception {
        // given
        Socio savedSocio = socioRepository.save(socio1);

        // when
        ResultActions response = mockMvc.perform(get("/api/socios/{id}", savedSocio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.nome", is(savedSocio.getNome())))
                .andExpect(jsonPath("$.email", is(savedSocio.getEmail())));
    }

    @Test
    void testBuscarSocioPorId_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/socios/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentId)));
    }

    @Test
    void testAtualizarSocio_Success() throws Exception {
        // given
        Socio savedSocio = socioRepository.save(socio1);

        SocioRequestDTO updatedDto = new SocioRequestDTO();
        updatedDto.setNome("Ana Silva");
        updatedDto.setTelefone("999");
        updatedDto.setEmail(savedSocio.getEmail()); // Keep email
        updatedDto.setDocumento(savedSocio.getDocumento()); // Keep doc
        updatedDto.setSenha("novasenha"); // Password update might be ignored by mapper/service logic here
        updatedDto.setCategoriaId(savedSocio.getCategoria().getId()); // Keep categoria

        // when
        ResultActions response = mockMvc.perform(put("/api/socios/{id}", savedSocio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.nome", is(updatedDto.getNome())))
                .andExpect(jsonPath("$.telefone", is(updatedDto.getTelefone())))
                .andExpect(jsonPath("$.email", is(savedSocio.getEmail()))); // Email shouldn't change unless logic allows
    }

    @Test
    void testAtualizarSocio_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(put("/api/socios/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentId)));
    }

    // Add tests for updating with existing email/documento similar to create tests

    @Test
    void testDeletarSocio_Success() throws Exception {
        // given
        Socio savedSocio = socioRepository.save(socio1);

        // when
        ResultActions response = mockMvc.perform(delete("/api/socios/{id}", savedSocio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify deletion
        assertFalse(socioRepository.findById(savedSocio.getId()).isPresent());
    }

    @Test
    void testDeletarSocio_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(delete("/api/socios/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentId)));
    }

     // TODO: Add test for deletarSocio_Fail_HasDependencies if validation is active
}
