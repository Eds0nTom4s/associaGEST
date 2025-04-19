package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
// LoginRequestDTO and AuthenticationResponseDTO no longer needed for setup
// import com.sistema.gestao.socios.dto.AuthenticationResponseDTO;
// import com.sistema.gestao.socios.dto.LoginRequestDTO;
import com.sistema.gestao.socios.dto.NotificacaoRequestDTO;
import com.sistema.gestao.socios.model.*;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.NotificacaoRepository;
import com.sistema.gestao.socios.repository.SocioRepository;
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
class NotificacaoControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin.notificacao@test.com";
    private static final String ADMIN_PASSWORD = "password";
    private String adminToken;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private SocioRepository socioRepository;

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

    private Socio savedSocio;
    private Notificacao notificacao1;
    private Notificacao notificacao2;
    private NotificacaoRequestDTO notificacaoRequestDTO;
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

        // Generate token directly using JwtService
        UserDetails userDetails = userDetailsService.loadUserByUsername(ADMIN_EMAIL);
        adminToken = jwtService.generateToken(userDetails);
    }

    @BeforeEach
    void setupTestData() {
        // Clean data repositories before each test
        notificacaoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Test data creation
        Categoria categoria = new Categoria(null, "Standard", "Básico", new BigDecimal("50.00"), null, null);
        categoriaRepository.save(categoria); // Salva a categoria antes de usá-la no sócio
        Socio socio = new Socio(null, "Test Socio Notif", "456", "test.notif@socio.com", "222", "pass", "ATIVO", categoria, null, null);
        savedSocio = socioRepository.save(socio);

        notificacao1 = new Notificacao(null, "AVISO", new Date(System.currentTimeMillis() - 50000), "Aviso importante", savedSocio);
        notificacao2 = new Notificacao(null, "PAGAMENTO", new Date(), "Pagamento pendente", savedSocio);

        notificacaoRequestDTO = new NotificacaoRequestDTO();
        notificacaoRequestDTO.setTipoNotificacao("EVENTO");
        notificacaoRequestDTO.setMensagem("Novo evento adicionado");
        notificacaoRequestDTO.setSocioId(savedSocio.getId());

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // --- Helper method to add Authorization header ---
    private String getAuthHeader() {
        return "Bearer " + adminToken;
    }

    @Test
    void testCriarNotificacao_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/notificacoes")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacaoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoNotificacao", is(notificacaoRequestDTO.getTipoNotificacao())))
                .andExpect(jsonPath("$.mensagem", is(notificacaoRequestDTO.getMensagem())))
                .andExpect(jsonPath("$.socioId", is(savedSocio.getId().intValue())))
                .andExpect(jsonPath("$.dataEnvio").isNotEmpty());
    }

    @Test
    void testCriarNotificacao_BadRequest_SocioNotFound() throws Exception {
        // given
        notificacaoRequestDTO.setSocioId(999L); // Non-existent ID

        // when
        ResultActions response = mockMvc.perform(post("/api/notificacoes")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacaoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isNotFound()) // Expect 404 as SocioService throws RecursoNaoEncontradoException
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: 999"))); // Expect lowercase 'id'
    }

     @Test
    void testCriarNotificacao_BadRequest_InvalidDTO() throws Exception {
        // given
        notificacaoRequestDTO.setTipoNotificacao(""); // Invalid type

        // when
        ResultActions response = mockMvc.perform(post("/api/notificacoes")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacaoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("tipoNotificacao")))
                .andExpect(jsonPath("$[0].message", is("Tipo da notificação não pode ser vazio")));
    }


    @Test
    void testListarTodasNotificacoes() throws Exception {
        // given
        List<Notificacao> notificacoes = new ArrayList<>();
        notificacoes.add(notificacao1);
        notificacoes.add(notificacao2);
        notificacaoRepository.saveAll(notificacoes);

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(notificacoes.size())));
    }

    @Test
    void testBuscarNotificacaoPorId_Success() throws Exception {
        // given
        Notificacao savedNotificacao = notificacaoRepository.save(notificacao1);

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/{id}", savedNotificacao.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(savedNotificacao.getId().intValue())))
                .andExpect(jsonPath("$.tipoNotificacao", is(savedNotificacao.getTipoNotificacao())));
    }

    @Test
    void testBuscarNotificacaoPorId_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/{id}", nonExistentId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Notificação não encontrada com id: " + nonExistentId)));
    }

    @Test
    void testBuscarNotificacoesPorSocioId() throws Exception {
        // given
        notificacaoRepository.save(notificacao1);
        notificacaoRepository.save(notificacao2);

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/socio/{socioId}", savedSocio.getId())
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].socioId", is(savedSocio.getId().intValue())))
                .andExpect(jsonPath("$[1].socioId", is(savedSocio.getId().intValue())));
    }

     @Test
    void testBuscarNotificacoesPorSocioId_SocioNotFound() throws Exception {
        // given
        long nonExistentSocioId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/socio/{socioId}", nonExistentSocioId)
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isNotFound()) // Expecting 404 from service check
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentSocioId)));
    }

    @Test
    void testBuscarNotificacoesPorTipo() throws Exception {
        // given
        notificacaoRepository.save(notificacao1); // AVISO
        notificacaoRepository.save(notificacao2); // PAGAMENTO

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/tipo/{tipo}", "AVISO")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader())); // Add Auth header

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoNotificacao", is("AVISO")));
    }

    @Test
    void testBuscarNotificacoesPorPeriodo() throws Exception {
        // given
        // Save notifications first to ensure they have IDs and potentially updated timestamps
        notificacaoRepository.save(notificacao1);
        notificacaoRepository.save(notificacao2);

        // Define a clear date range that encompasses the test data creation time
        // Assuming notificacao1 and notificacao2 are created very close together in @BeforeEach
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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/periodo")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                // Expect size might still be 2 if both notifications fall within the adjusted day range
                .andExpect(jsonPath("$", hasSize(2)));
    }

     @Test
    void testBuscarNotificacoesPorPeriodo_InvalidDates() throws Exception {
        // given
        // Use clearly distinct dates in the wrong order
        String inicioStr = "2025-04-07";
        String fimStr = "2025-04-06"; // End date is before start date

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/periodo")
                .header(HttpHeaders.AUTHORIZATION, getAuthHeader()) // Add Auth header
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Período de datas inválido.")));
    }

    // DELETE/PUT tests are likely not applicable for notifications
}
