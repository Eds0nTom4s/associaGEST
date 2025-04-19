package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.RelatorioFinanceiroRequestDTO;
import com.sistema.gestao.socios.model.RelatorioFinanceiro;
import com.sistema.gestao.socios.model.Role; // Import Role
import com.sistema.gestao.socios.model.Usuario; // Import Usuario
import com.sistema.gestao.socios.repository.*; // Import all repositories including UsuarioRepository
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance; // Import TestInstance
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders; // Import HttpHeaders
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult; // Import MvcResult
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Use PER_CLASS for @BeforeAll
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Changed to MOCK environment
@AutoConfigureMockMvc
class RelatorioFinanceiroControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.relatorio@test.com";
    private static final String ADMIN_PASSWORD = "password";
    private String adminToken; // Store the token

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatorioFinanceiroRepository relatorioRepository;

    // Injetar outros repositórios para limpeza completa
    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // Inject UsuarioRepository

    @Autowired
    private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Autowired
    private ObjectMapper objectMapper;

    private RelatorioFinanceiro relatorio1;
    private RelatorioFinanceiro relatorio2;
    private RelatorioFinanceiroRequestDTO relatorioRequestDTO;
    private SimpleDateFormat dateFormat;

    @BeforeAll
    void setupAuthentication() throws Exception {
        // Clean user repo before creating the admin user
        usuarioRepository.deleteAll();

        // Create Admin User for authentication
        Usuario adminUser = Usuario.builder()
                .email(ADMIN_EMAIL)
                .senha(passwordEncoder.encode(ADMIN_PASSWORD))
                .role(Role.ADMIN)
                .build();
        usuarioRepository.save(adminUser);

        // Perform login request to get the token
        LoginRequestDTO loginRequest = new LoginRequestDTO(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthenticationResponseDTO authResponse = objectMapper.readValue(responseBody, AuthenticationResponseDTO.class);
        adminToken = authResponse.getToken(); // Store the token
    }

    @BeforeEach
    void setupTestData() {
        // Clean data repositories before each test
        relatorioRepository.deleteAll();
        pagamentoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Test data creation
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date inicio1 = new Date(System.currentTimeMillis() - 200000);
        Date fim1 = new Date(System.currentTimeMillis() - 100000);
        Date inicio2 = new Date(System.currentTimeMillis() - 100000);
        Date fim2 = new Date();

        relatorio1 = new RelatorioFinanceiro(null, "MENSAL", inicio1, fim1, "Dados Relatório 1");
        relatorio2 = new RelatorioFinanceiro(null, "SEMANAL", inicio2, fim2, "Dados Relatório 2");

        relatorioRequestDTO = new RelatorioFinanceiroRequestDTO();
        relatorioRequestDTO.setTipoRelatorio("ANUAL");
        relatorioRequestDTO.setPeriodoInicio(inicio1);
        relatorioRequestDTO.setPeriodoFim(fim2);
    }

    // --- Helper method to add Authorization header ---
    private String getAuthHeader() {
        return "Bearer " + adminToken;
    }

    @Test
    void testGerarRelatorio_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/relatorios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relatorioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoRelatorio", is(relatorioRequestDTO.getTipoRelatorio())))
                // Compare date strings as JSON might format them differently
                .andExpect(jsonPath("$.periodoInicio", is(dateFormat.format(relatorioRequestDTO.getPeriodoInicio()))))
                .andExpect(jsonPath("$.periodoFim", is(dateFormat.format(relatorioRequestDTO.getPeriodoFim()))))
                .andExpect(jsonPath("$.dadosRelatorio", containsString(relatorioRequestDTO.getTipoRelatorio()))); // Check placeholder data
    }

    @Test
    void testGerarRelatorio_BadRequest_InvalidDates() throws Exception {
        // given
        // Use clearly distinct dates in the wrong order by setting Date objects
        // that will serialize to different "yyyy-MM-dd" strings
        Date inicioDate = dateFormat.parse("2025-04-07");
        Date fimDate = dateFormat.parse("2025-04-06"); // End date is before start date
        relatorioRequestDTO.setPeriodoInicio(inicioDate);
        relatorioRequestDTO.setPeriodoFim(fimDate);

        // when
        ResultActions response = mockMvc.perform(post("/api/relatorios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relatorioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Período de datas inválido para o relatório.")));
    }

     @Test
    void testGerarRelatorio_BadRequest_InvalidDTO() throws Exception {
        // given
        relatorioRequestDTO.setTipoRelatorio(""); // Invalid type

        // when
        ResultActions response = mockMvc.perform(post("/api/relatorios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relatorioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("tipoRelatorio")))
                .andExpect(jsonPath("$[0].message", is("Tipo do relatório não pode ser vazio")));
    }


    @Test
    void testListarTodosRelatorios() throws Exception {
        // given
        List<RelatorioFinanceiro> relatorios = new ArrayList<>();
        relatorios.add(relatorio1);
        relatorios.add(relatorio2);
        relatorioRepository.saveAll(relatorios);

        // when
        ResultActions response = mockMvc.perform(get("/api/relatorios")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(relatorios.size())))
                .andExpect(jsonPath("$[0].tipoRelatorio", is(relatorio1.getTipoRelatorio())))
                .andExpect(jsonPath("$[1].tipoRelatorio", is(relatorio2.getTipoRelatorio())));
    }

    @Test
    void testBuscarRelatorioPorId_Success() throws Exception {
        // given
        RelatorioFinanceiro savedRelatorio = relatorioRepository.save(relatorio1);

        // when
        ResultActions response = mockMvc.perform(get("/api/relatorios/{id}", savedRelatorio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(savedRelatorio.getId().intValue())))
                .andExpect(jsonPath("$.tipoRelatorio", is(savedRelatorio.getTipoRelatorio())));
    }

    @Test
    void testBuscarRelatorioPorId_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/relatorios/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Relatório não encontrado com id: " + nonExistentId)));
    }

    // Similar tests for buscarPorTipo and buscarPorPeriodo

    @Test
    void testDeletarRelatorio_Success() throws Exception {
        // given
        RelatorioFinanceiro savedRelatorio = relatorioRepository.save(relatorio1);

        // when
        ResultActions response = mockMvc.perform(delete("/api/relatorios/{id}", savedRelatorio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify deletion
        assertFalse(relatorioRepository.findById(savedRelatorio.getId()).isPresent());
    }

    @Test
    void testDeletarRelatorio_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(delete("/api/relatorios/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Relatório não encontrado com id: " + nonExistentId)));
    }
}
