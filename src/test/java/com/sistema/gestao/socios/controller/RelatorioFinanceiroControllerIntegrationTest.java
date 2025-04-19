package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.RelatorioFinanceiroRequestDTO;
import com.sistema.gestao.socios.model.RelatorioFinanceiro;
import com.sistema.gestao.socios.repository.CategoriaRepository; // Import adicionado
import com.sistema.gestao.socios.repository.PagamentoRepository; // Import adicionado
import com.sistema.gestao.socios.repository.RelatorioFinanceiroRepository;
import com.sistema.gestao.socios.repository.SocioRepository; // Import adicionado
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
// import org.springframework.test.annotation.DirtiesContext; // Removido
import org.springframework.test.web.servlet.MockMvc;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Removido
class RelatorioFinanceiroControllerIntegrationTest {

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
    private ObjectMapper objectMapper;

    private RelatorioFinanceiro relatorio1;
    private RelatorioFinanceiro relatorio2;
    private RelatorioFinanceiroRequestDTO relatorioRequestDTO;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        relatorioRepository.deleteAll();
        pagamentoRepository.deleteAll(); // Adicionado
        socioRepository.deleteAll();     // Adicionado
        categoriaRepository.deleteAll(); // Adicionado

        // Criação de dados de teste
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

    @Test
    void testGerarRelatorio_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/relatorios")
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
        relatorioRequestDTO.setPeriodoFim(new Date(relatorioRequestDTO.getPeriodoInicio().getTime() - 1000)); // End before start

        // when
        ResultActions response = mockMvc.perform(post("/api/relatorios")
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
        ResultActions response = mockMvc.perform(get("/api/relatorios"));

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
        ResultActions response = mockMvc.perform(get("/api/relatorios/{id}", savedRelatorio.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/relatorios/{id}", nonExistentId));

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
        ResultActions response = mockMvc.perform(delete("/api/relatorios/{id}", savedRelatorio.getId()));

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
        ResultActions response = mockMvc.perform(delete("/api/relatorios/{id}", nonExistentId));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Relatório não encontrado com id: " + nonExistentId)));
    }
}
