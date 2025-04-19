package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.PagamentoRequestDTO;
import com.sistema.gestao.socios.model.*; // Import all models including Usuario, Role
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.PagamentoRepository;
import com.sistema.gestao.socios.repository.SocioRepository;
import com.sistema.gestao.socios.repository.UsuarioRepository; // Import UsuarioRepository
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Use PER_CLASS for @BeforeAll
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Changed to MOCK environment
@AutoConfigureMockMvc
class PagamentoControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.pagamento@test.com";
    private static final String ADMIN_PASSWORD = "password";
    private String adminToken; // Store the token

    @Autowired
    private MockMvc mockMvc;

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

    private Categoria savedCategoria;
    private Socio savedSocio;
    private Pagamento pagamento1;
    private Pagamento pagamento2;
    private PagamentoRequestDTO pagamentoRequestDTO;
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
        pagamentoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Test data creation
        Categoria categoria = new Categoria(null, "Standard", "Básico", new BigDecimal("50.00"), null, null);
        savedCategoria = categoriaRepository.save(categoria);
        Socio socio = new Socio(null, "Test Socio", "123", "test@socio.com", "111", "pass", "PENDENTE", savedCategoria, null, null);
        savedSocio = socioRepository.save(socio);

        pagamento1 = new Pagamento(null, new Date(System.currentTimeMillis() - 50000), new BigDecimal("50.00"), "CONFIRMADO", savedSocio, savedCategoria);
        pagamento2 = new Pagamento(null, new Date(), new BigDecimal("50.00"), "PENDENTE", savedSocio, savedCategoria);

        pagamentoRequestDTO = new PagamentoRequestDTO();
        pagamentoRequestDTO.setValorPago(new BigDecimal("50.00"));
        pagamentoRequestDTO.setStatus("CONFIRMADO");
        pagamentoRequestDTO.setSocioId(savedSocio.getId());
        pagamentoRequestDTO.setCategoriaId(savedCategoria.getId());

        // Setup date formatter for query params
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Ensure consistency
    }

    // --- Helper method to add Authorization header ---
    private String getAuthHeader() {
        return "Bearer " + adminToken;
    }

    @Test
    void testRegistrarPagamento_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/pagamentos")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorPago", is(50.00)))
                .andExpect(jsonPath("$.status", is("CONFIRMADO")))
                .andExpect(jsonPath("$.socioId", is(savedSocio.getId().intValue())))
                .andExpect(jsonPath("$.categoriaId", is(savedCategoria.getId().intValue())))
                .andExpect(jsonPath("$.dataPagamento").isNotEmpty()); // Check date is set
    }

    @Test
    void testRegistrarPagamento_BadRequest_SocioNotFound() throws Exception {
        // given
        pagamentoRequestDTO.setSocioId(999L); // Non-existent ID

        // when
        ResultActions response = mockMvc.perform(post("/api/pagamentos")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound()) // Expect 404 as SocioService throws RecursoNaoEncontradoException
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: 999"))); // Expect lowercase 'id'
    }

     @Test
    void testRegistrarPagamento_BadRequest_CategoriaNotFound() throws Exception {
        // given
        pagamentoRequestDTO.setCategoriaId(999L); // Non-existent ID

        // when
        ResultActions response = mockMvc.perform(post("/api/pagamentos")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound()) // Expect 404 as CategoriaService throws RecursoNaoEncontradoException
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: 999"))); // Expect lowercase 'id'
    }

    @Test
    void testListarTodosPagamentos() throws Exception {
        // given
        List<Pagamento> pagamentos = new ArrayList<>();
        pagamentos.add(pagamento1);
        pagamentos.add(pagamento2);
        pagamentoRepository.saveAll(pagamentos);

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(pagamentos.size())));
    }

    @Test
    void testBuscarPagamentoPorId_Success() throws Exception {
        // given
        Pagamento savedPagamento = pagamentoRepository.save(pagamento1);

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/{id}", savedPagamento.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(savedPagamento.getId().intValue())))
                .andExpect(jsonPath("$.status", is(savedPagamento.getStatus())));
    }

    @Test
    void testBuscarPagamentoPorId_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Pagamento não encontrado com id: " + nonExistentId)));
    }

    @Test
    void testBuscarPagamentosPorSocioId() throws Exception {
        // given
        pagamentoRepository.save(pagamento1);
        pagamentoRepository.save(pagamento2);

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/socio/{socioId}", savedSocio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].socioId", is(savedSocio.getId().intValue())))
                .andExpect(jsonPath("$[1].socioId", is(savedSocio.getId().intValue())));
    }

     @Test
    void testBuscarPagamentosPorSocioId_SocioNotFound() throws Exception {
        // given
        long nonExistentSocioId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/socio/{socioId}", nonExistentSocioId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound()) // Expecting 404 from service check
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentSocioId)));
    }

    // Similar tests for buscarPorCategoriaId

    @Test
    void testBuscarPagamentosPorStatus() throws Exception {
        // given
        pagamentoRepository.save(pagamento1); // CONFIRMADO
        pagamentoRepository.save(pagamento2); // PENDENTE

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/status/{status}", "CONFIRMADO")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMADO")));
    }

    @Test
    void testBuscarPagamentosPorPeriodo() throws Exception {
        // given
        // Save pagamentos first
        pagamentoRepository.save(pagamento1);
        pagamentoRepository.save(pagamento2);

        // Define a clear date range that encompasses the test data creation time
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // Start date: Beginning of the day the test runs
        cal.setTime(new Date()); // Use current time as a reference
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        Date inicioRange = cal.getTime();

        // End date: Beginning of the next day
        cal.add(java.util.Calendar.DATE, 1);
        Date fimRange = cal.getTime();

        String inicioStr = dateFormat.format(inicioRange);
        String fimStr = dateFormat.format(fimRange);

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/periodo")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2))); // Both should be within the adjusted day range
    }

     @Test
    void testBuscarPagamentosPorPeriodo_InvalidDates() throws Exception {
        // given
        // Use clearly distinct dates in the wrong order
        String inicioStr = "2025-04-07";
        String fimStr = "2025-04-06"; // End date is before start date

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/periodo")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Período de datas inválido.")));
    }

    @Test
    void testAtualizarStatusPagamento_Success() throws Exception {
        // given
        Pagamento savedPagamento = pagamentoRepository.save(pagamento2); // PENDENTE
        String novoStatus = "CONFIRMADO";

        // when
        ResultActions response = mockMvc.perform(put("/api/pagamentos/{id}/status", savedPagamento.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("novoStatus", novoStatus));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(savedPagamento.getId().intValue())))
                .andExpect(jsonPath("$.status", is(novoStatus)));
    }

     @Test
    void testAtualizarStatusPagamento_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;
        String novoStatus = "CONFIRMADO";

        // when
        ResultActions response = mockMvc.perform(put("/api/pagamentos/{id}/status", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("novoStatus", novoStatus));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Pagamento não encontrado com id: " + nonExistentId)));
    }

     @Test
    void testAtualizarStatusPagamento_BadRequest_StatusEmpty() throws Exception {
        // given
        Pagamento savedPagamento = pagamentoRepository.save(pagamento2);
        String novoStatus = ""; // Empty status

        // when
        ResultActions response = mockMvc.perform(put("/api/pagamentos/{id}/status", savedPagamento.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("novoStatus", novoStatus));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Novo status não pode ser vazio.")));
    }
}
