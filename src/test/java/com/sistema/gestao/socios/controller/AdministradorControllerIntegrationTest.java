package com.sistema.gestao.socios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.gestao.socios.dto.AdministradorRequestDTO;
import com.sistema.gestao.socios.model.Administrador;
import com.sistema.gestao.socios.repository.AdministradorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
// import org.springframework.test.annotation.DirtiesContext; // Removido
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Removido
class AdministradorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Administrador admin1;
    private Administrador admin2;
    private AdministradorRequestDTO adminRequestDTO;

    @BeforeEach
    void setup() {
        // Limpeza explícita do banco de dados antes de cada teste
        administradorRepository.deleteAll();

        // Criação de dados de teste
        admin1 = new Administrador(null, "Admin One", "admin1@example.com", "pass1");
        admin2 = new Administrador(null, "Admin Two", "admin2@example.com", "pass2");

        adminRequestDTO = new AdministradorRequestDTO();
        adminRequestDTO.setNome("Admin Three");
        adminRequestDTO.setEmail("admin3@example.com");
        adminRequestDTO.setSenha("senhaforte123");
    }

    @Test
    void testCadastrarAdministrador_Success() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/administradores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is(adminRequestDTO.getNome())))
                .andExpect(jsonPath("$.email", is(adminRequestDTO.getEmail())));
                // Password should not be returned
    }

    @Test
    void testCadastrarAdministrador_BadRequest_EmailExists() throws Exception {
        // given
        administradorRepository.save(admin1); // Save admin1 first
        adminRequestDTO.setEmail(admin1.getEmail()); // Use existing email

        // when
        ResultActions response = mockMvc.perform(post("/api/administradores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email de administrador já cadastrado: " + admin1.getEmail())));
    }

     @Test
    void testCadastrarAdministrador_BadRequest_InvalidDTO() throws Exception {
        // given
        adminRequestDTO.setEmail("invalid-email"); // Invalid email format

        // when
        ResultActions response = mockMvc.perform(post("/api/administradores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequestDTO)));

        // then
        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field", is("email")))
                .andExpect(jsonPath("$[0].message", is("Formato de email inválido")));
    }

    @Test
    void testListarTodosAdministradores() throws Exception {
        // given
        List<Administrador> admins = new ArrayList<>();
        admins.add(admin1);
        admins.add(admin2);
        administradorRepository.saveAll(admins);

        // when
        ResultActions response = mockMvc.perform(get("/api/administradores"));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(admins.size())))
                .andExpect(jsonPath("$[0].email", is(admin1.getEmail())))
                .andExpect(jsonPath("$[1].email", is(admin2.getEmail())));
    }

    @Test
    void testBuscarAdministradorPorId_Success() throws Exception {
        // given
        Administrador savedAdmin = administradorRepository.save(admin1);

        // when
        ResultActions response = mockMvc.perform(get("/api/administradores/{id}", savedAdmin.getId()));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(savedAdmin.getId().intValue())))
                .andExpect(jsonPath("$.email", is(savedAdmin.getEmail())));
    }

    @Test
    void testBuscarAdministradorPorId_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(get("/api/administradores/{id}", nonExistentId));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Administrador não encontrado com id: " + nonExistentId)));
    }

    @Test
    void testAtualizarAdministrador_Success() throws Exception {
        // given
        Administrador savedAdmin = administradorRepository.save(admin1);

        AdministradorRequestDTO updatedDto = new AdministradorRequestDTO();
        updatedDto.setNome("Admin One Updated");
        updatedDto.setEmail(savedAdmin.getEmail()); // Keep email
        updatedDto.setSenha("ignored"); // Password update should be handled separately

        // when
        ResultActions response = mockMvc.perform(put("/api/administradores/{id}", savedAdmin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.nome", is(updatedDto.getNome())))
                .andExpect(jsonPath("$.email", is(savedAdmin.getEmail())));
    }

    @Test
    void testAtualizarAdministrador_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(put("/api/administradores/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequestDTO)));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Administrador não encontrado com id: " + nonExistentId)));
    }

     @Test
    void testAtualizarAdministrador_BadRequest_EmailExists() throws Exception {
        // given
        administradorRepository.save(admin1); // admin1@example.com
        Administrador savedAdmin2 = administradorRepository.save(admin2); // admin2@example.com

        AdministradorRequestDTO updateDto = new AdministradorRequestDTO();
        updateDto.setNome("Admin Two Updated");
        updateDto.setEmail(admin1.getEmail()); // Try to update admin2's email to admin1's email
        updateDto.setSenha("ignored");

        // when
        ResultActions response = mockMvc.perform(put("/api/administradores/{id}", savedAdmin2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Email já cadastrado para outro administrador: " + admin1.getEmail())));
    }


    @Test
    void testDeletarAdministrador_Success() throws Exception {
        // given
        Administrador savedAdmin = administradorRepository.save(admin1);
        administradorRepository.save(admin2); // Ensure not deleting the last one if validation exists

        // when
        ResultActions response = mockMvc.perform(delete("/api/administradores/{id}", savedAdmin.getId()));

        // then
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify deletion
        assertFalse(administradorRepository.findById(savedAdmin.getId()).isPresent());
    }

    @Test
    void testDeletarAdministrador_NotFound() throws Exception {
        // given
        long nonExistentId = 999L;

        // when
        ResultActions response = mockMvc.perform(delete("/api/administradores/{id}", nonExistentId));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.message", is("Administrador não encontrado com id: " + nonExistentId)));
    }

    // TODO: Add test for deletarAdministrador_Fail_LastAdmin if validation is active
}
