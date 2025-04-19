package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.CategoriaRequestDTO;
// LoginRequestDTO and AuthenticationResponseDTO no longer needed for setup
// import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
// import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Role;
import com.sistema.gestao.socios.model.Usuario;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.UsuarioRepository;
import com.sistema.gestao.socios.security.JwtService; // Import JwtService
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance; // Import TestInstance
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Import UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
// MvcResult no longer needed
// import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse; // Add missing import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Use PER_CLASS for @BeforeAll
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Changed to MOCK environment
@AutoConfigureMockMvc
class CategoriaControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.categoria@test.com";
    private static final String ADMIN_PASSWORD = "password";
    private String adminToken;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inject UsuarioRepository

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService; // Inject JwtService

    @Autowired
    private UserDetailsService userDetailsService; // Inject UserDetailsService

    private Categoria categoria1;
    private Categoria categoria2;
    private CategoriaRequestDTO categoriaRequestDTO;

    @BeforeAll
    void setupAuthentication() throws Exception {
        // Clean user repo before creating the admin user
        usuarioRepository.deleteAll();

        // Create Admin User for authentication
        Usuario adminUser = Usuario.builder()
                // .nome("Admin Categoria Test") // Removed - Usuario doesn't have 'nome' field
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
        // Clean only categoria repository before each test
        categoriaRepository.deleteAll();

        // Test data creation
        categoria1 = new Categoria(null, "Standard", "Básico", new BigDecimal("50.00"), null, null);
        categoria2 = new Categoria(null, "Premium", "Completo", new BigDecimal("100.00"), null, null);

        categoriaRequestDTO = new CategoriaRequestDTO();
        categoriaRequestDTO.setNome("VIP");
        categoriaRequestDTO.setBeneficios("Exclusivo");
        categoriaRequestDTO.setValorMensalidade(new BigDecimal("200.00"));
    }

    // --- Helper method to add Authorization header ---
    private String getAuthHeader() {
        return "Bearer " + adminToken;
    }

    @Test
    void testCadastrarCategoria() throws Exception {
        // when - action
        ResultActions response = mockMvc.perform(post("/api/categorias")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRequestDTO)));

        // then - verify the output
        response.andDo(print()) // Print response for debugging
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is(categoriaRequestDTO.getNome())))
                .andExpect(jsonPath("$.beneficios", is(categoriaRequestDTO.getBeneficios())))
                .andExpect(jsonPath("$.valorMensalidade", is(200.00))); // Match numeric value
    }

     @Test
    void testCadastrarCategoria_BadRequest_NomeExistente() throws Exception {
        // given - precondition or setup
        categoriaRepository.save(categoria1); // Save "Standard" first

        CategoriaRequestDTO duplicateDto = new CategoriaRequestDTO();
        duplicateDto.setNome("Standard"); // Same name
        duplicateDto.setBeneficios("...");
        duplicateDto.setValorMensalidade(BigDecimal.TEN);

        // when - action
        ResultActions response = mockMvc.perform(post("/api/categorias")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateDto)));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isBadRequest()) // Expecting 400 due to RegraNegocioException
                .andExpect(jsonPath("$.message", is("Nome da categoria já existe: Standard")));
    }

    @Test
    void testListarTodasCategorias() throws Exception {
        // given - precondition or setup
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(categoria1);
        categorias.add(categoria2);
        categoriaRepository.saveAll(categorias);

        // when - action
        ResultActions response = mockMvc.perform(get("/api/categorias")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(categorias.size())))
                .andExpect(jsonPath("$[0].nome", is(categoria1.getNome())))
                .andExpect(jsonPath("$[1].nome", is(categoria2.getNome())));
    }

    @Test
    void testBuscarCategoriaPorId_Success() throws Exception {
        // given - precondition or setup
        Categoria savedCategoria = categoriaRepository.save(categoria1);

        // when - action
        ResultActions response = mockMvc.perform(get("/api/categorias/{id}", savedCategoria.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.nome", is(savedCategoria.getNome())))
                .andExpect(jsonPath("$.beneficios", is(savedCategoria.getBeneficios())));
    }

    @Test
    void testBuscarCategoriaPorId_NotFound() throws Exception {
        // given - precondition or setup
        long nonExistentId = 999L;

        // when - action
        ResultActions response = mockMvc.perform(get("/api/categorias/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId))); // Expect lowercase 'id'
    }

    @Test
    void testAtualizarCategoria_Success() throws Exception {
        // given - precondition or setup
        Categoria savedCategoria = categoriaRepository.save(categoria1);

        CategoriaRequestDTO updatedDto = new CategoriaRequestDTO();
        updatedDto.setNome("Standard Plus");
        updatedDto.setBeneficios("Básico + Email");
        updatedDto.setValorMensalidade(new BigDecimal("60.00"));

        // when - action
        ResultActions response = mockMvc.perform(put("/api/categorias/{id}", savedCategoria.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)));

        // then - verify the output
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.nome", is(updatedDto.getNome())))
                .andExpect(jsonPath("$.beneficios", is(updatedDto.getBeneficios())))
                .andExpect(jsonPath("$.valorMensalidade", is(60.00)));
    }

    @Test
    void testAtualizarCategoria_NotFound() throws Exception {
        // given - precondition or setup
        long nonExistentId = 999L;

        // when - action
        ResultActions response = mockMvc.perform(put("/api/categorias/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRequestDTO)));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId))); // Expect lowercase 'id'
    }

     @Test
    void testAtualizarCategoria_BadRequest_NomeExistente() throws Exception {
        // given - precondition or setup
        categoriaRepository.save(categoria1); // Save "Standard"
        Categoria savedCategoria2 = categoriaRepository.save(categoria2); // Save "Premium"

        CategoriaRequestDTO updateDto = new CategoriaRequestDTO();
        updateDto.setNome("Standard"); // Trying to update Premium to Standard (which exists)
        updateDto.setBeneficios("...");
        updateDto.setValorMensalidade(BigDecimal.TEN);


        // when - action
        ResultActions response = mockMvc.perform(put("/api/categorias/{id}", savedCategoria2.getId()) // Update Premium (ID 2)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // then - verify the output
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Nome da categoria já existe: Standard")));
    }


    @Test
    void testDeletarCategoria_Success() throws Exception {
        // given - precondition or setup
        Categoria savedCategoria = categoriaRepository.save(categoria1);

        // when - action
        ResultActions response = mockMvc.perform(delete("/api/categorias/{id}", savedCategoria.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then - verify the output
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify deletion in repository
        assertFalse(categoriaRepository.findById(savedCategoria.getId()).isPresent());
    }

    @Test
    void testDeletarCategoria_NotFound() throws Exception {
        // given - precondition or setup
        long nonExistentId = 999L;

        // when - action
        ResultActions response = mockMvc.perform(delete("/api/categorias/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId))); // Expect lowercase 'id'
    }

    // TODO: Add test for deletarCategoria_Fail_HasSocios if that validation is active
}
