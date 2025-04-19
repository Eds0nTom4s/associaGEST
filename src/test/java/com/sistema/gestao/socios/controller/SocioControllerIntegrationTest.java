package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import com.sistema.gestao.socios.repository.PagamentoRepository; // Import adicionado
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Removido em favor da limpeza explícita
class SocioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository; // Injetado

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria savedCategoria;
    private Socio socio1;
    private Socio socio2;
    private SocioRequestDTO socioRequestDTO;

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        pagamentoRepository.deleteAll(); // Adicionado
        socioRepository.deleteAll();
        categoriaRepository.deleteAll();


        // Criação de dados de teste
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

    @Test
    void testCadastrarSocio_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socioRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest()) // Or NotFound depending on how controller throws
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com ID: 999")));
    }

     @Test
    void testCadastrarSocio_BadRequest_EmailExists() throws Exception {
        // given
        socioRepository.save(socio1); // Save Ana with email ana@test.com
        socioRequestDTO.setEmail("ana@test.com"); // Try to save Carlos with Ana's email

        // when
        ResultActions response = mockMvc.perform(post("/api/socios")
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
        ResultActions response = mockMvc.perform(get("/api/socios"));

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
        ResultActions response = mockMvc.perform(get("/api/socios/{id}", savedSocio.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/socios/{id}", nonExistentId));

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
        ResultActions response = mockMvc.perform(delete("/api/socios/{id}", savedSocio.getId()));

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
        ResultActions response = mockMvc.perform(delete("/api/socios/{id}", nonExistentId));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Sócio não encontrado com id: " + nonExistentId)));
    }

     // TODO: Add test for deletarSocio_Fail_HasDependencies if validation is active
}
