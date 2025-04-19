package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.CategoriaRequestDTO;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.repository.CategoriaRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse; // Add missing import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Removido em favor da limpeza explícita
class CategoriaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper; // For JSON conversion

    private Categoria categoria1;
    private Categoria categoria2;
    private CategoriaRequestDTO categoriaRequestDTO;

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        categoriaRepository.deleteAll();

        // Criação de dados de teste
        categoria1 = new Categoria(null, "Standard", "Básico", new BigDecimal("50.00"), null, null);
        categoria2 = new Categoria(null, "Premium", "Completo", new BigDecimal("100.00"), null, null);

        categoriaRequestDTO = new CategoriaRequestDTO();
        categoriaRequestDTO.setNome("VIP");
        categoriaRequestDTO.setBeneficios("Exclusivo");
        categoriaRequestDTO.setValorMensalidade(new BigDecimal("200.00"));
    }

    @Test
    void testCadastrarCategoria() throws Exception {
        // when - action
        ResultActions response = mockMvc.perform(post("/api/categorias")
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
        ResultActions response = mockMvc.perform(get("/api/categorias"));

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
        ResultActions response = mockMvc.perform(get("/api/categorias/{id}", savedCategoria.getId()));

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
        ResultActions response = mockMvc.perform(get("/api/categorias/{id}", nonExistentId));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId)));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRequestDTO)));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId)));
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
        ResultActions response = mockMvc.perform(delete("/api/categorias/{id}", savedCategoria.getId()));

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
        ResultActions response = mockMvc.perform(delete("/api/categorias/{id}", nonExistentId));

        // then - verify the output
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Categoria não encontrada com id: " + nonExistentId)));
    }

    // TODO: Add test for deletarCategoria_Fail_HasSocios if that validation is active
}
