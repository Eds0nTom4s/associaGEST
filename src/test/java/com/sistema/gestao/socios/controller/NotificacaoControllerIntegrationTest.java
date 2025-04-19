package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.NotificacaoRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Notificacao;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.NotificacaoRepository;
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
class NotificacaoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Socio savedSocio;
    private Notificacao notificacao1;
    private Notificacao notificacao2;
    private NotificacaoRequestDTO notificacaoRequestDTO;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        notificacaoRepository.deleteAll();
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();

        // Criação de dados de teste (dependências primeiro)
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

    @Test
    void testCriarNotificacao_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/notificacoes")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacaoRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com ID: 999")));
    }

     @Test
    void testCriarNotificacao_BadRequest_InvalidDTO() throws Exception {
        // given
        notificacaoRequestDTO.setTipoNotificacao(""); // Invalid type

        // when
        ResultActions response = mockMvc.perform(post("/api/notificacoes")
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
        ResultActions response = mockMvc.perform(get("/api/notificacoes"));

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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/{id}", savedNotificacao.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/{id}", nonExistentId));

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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/socio/{socioId}", savedSocio.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/socio/{socioId}", nonExistentSocioId));

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
        ResultActions response = mockMvc.perform(get("/api/notificacoes/tipo/{tipo}", "AVISO"));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipoNotificacao", is("AVISO")));
    }

    @Test
    void testBuscarNotificacoesPorPeriodo() throws Exception {
        // given
        notificacaoRepository.save(notificacao1);
        notificacaoRepository.save(notificacao2);
        String inicioStr = dateFormat.format(new Date(System.currentTimeMillis() - 60000)); // 1 min ago
        String fimStr = dateFormat.format(new Date(System.currentTimeMillis() + 1000)); // Now + 1 sec

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/periodo")
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$", hasSize(2)));
    }

     @Test
    void testBuscarNotificacoesPorPeriodo_InvalidDates() throws Exception {
        // given
        String inicioStr = dateFormat.format(new Date());
        String fimStr = dateFormat.format(new Date(System.currentTimeMillis() - 60000)); // End before start

        // when
        ResultActions response = mockMvc.perform(get("/api/notificacoes/periodo")
                .param("inicio", inicioStr)
                .param("fim", fimStr));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Período de datas inválido.")));
    }

    // DELETE/PUT tests are likely not applicable for notifications
}
