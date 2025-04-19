package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.PagamentoRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Pagamento;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.PagamentoRepository;
import com.sistema.gestao.socios.repository.SocioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
// import org.springframework.test.annotation.DirtiesContext; // Removido
import org.springframework.test.web.servlet.MockMvc;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Removido
class PagamentoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria savedCategoria;
    private Socio savedSocio;
    private Pagamento pagamento1;
    private Pagamento pagamento2;
    private PagamentoRequestDTO pagamentoRequestDTO;
    private SimpleDateFormat dateFormat; // For date formatting in query params

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        pagamentoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Criação de dados de teste (dependências primeiro)
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

    @Test
    void testRegistrarPagamento_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/pagamentos")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com ID: 999")));
    }

     @Test
    void testRegistrarPagamento_BadRequest_CategoriaNotFound() throws Exception {
        // given
        pagamentoRequestDTO.setCategoriaId(999L); // Non-existent ID

        // when
        ResultActions response = mockMvc.perform(post("/api/pagamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagamentoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com ID: 999")));
    }

    @Test
    void testListarTodosPagamentos() throws Exception {
        // given
        List<Pagamento> pagamentos = new ArrayList<>();
        pagamentos.add(pagamento1);
        pagamentos.add(pagamento2);
        pagamentoRepository.saveAll(pagamentos);

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos"));

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
        ResultActions response = mockMvc.perform(get("/api/pagamentos/{id}", savedPagamento.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/pagamentos/{id}", nonExistentId));

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
        ResultActions response = mockMvc.perform(get("/api/pagamentos/socio/{socioId}", savedSocio.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/pagamentos/socio/{socioId}", nonExistentSocioId));

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
        ResultActions response = mockMvc.perform(get("/api/pagamentos/status/{status}", "CONFIRMADO"));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("CONFIRMADO")));
    }

    @Test
    void testBuscarPagamentosPorPeriodo() throws Exception {
        // given
        pagamentoRepository.save(pagamento1);
        pagamentoRepository.save(pagamento2);
        String inicioStr = dateFormat.format(new Date(System.currentTimeMillis() - 60000)); // 1 min ago
        String fimStr = dateFormat.format(new Date(System.currentTimeMillis() + 1000)); // Now + 1 sec

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/periodo")
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2))); // Both should be within the last minute
    }

     @Test
    void testBuscarPagamentosPorPeriodo_InvalidDates() throws Exception {
        // given
        String inicioStr = dateFormat.format(new Date());
        String fimStr = dateFormat.format(new Date(System.currentTimeMillis() - 60000)); // End before start

        // when
        ResultActions response = mockMvc.perform(get("/api/pagamentos/periodo")
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
                .param("novoStatus", novoStatus));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Novo status não pode ser vazio.")));
    }
}
