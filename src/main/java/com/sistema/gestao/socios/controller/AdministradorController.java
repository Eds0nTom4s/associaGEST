package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.AdministradorRequestDTO;
import com.sistema.gestao.socios.dto.AdministradorResponseDTO;
import com.sistema.gestao.socios.mapper.AdministradorMapper;
import com.sistema.gestao.socios.model.Administrador;
import com.sistema.gestao.socios.service.AdministradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// ResponseStatusException can be removed as GlobalExceptionHandler handles custom exceptions
// import org.springframework.web.server.ResponseStatusException;

import java.util.List;
// Optional is no longer returned by the refactored service method
// import java.util.Optional;

@RestController
@RequestMapping("/api/administradores")
@Tag(name = "Administradores", description = "API para gerenciamento de administradores")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private AdministradorMapper administradorMapper;

    @Operation(summary = "Listar todos os administradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administradores listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<AdministradorResponseDTO>> listarTodos() {
        List<Administrador> administradores = administradorService.listarTodos();
        List<AdministradorResponseDTO> dtos = administradorMapper.toAdministradorResponseDTOList(administradores);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar administrador por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administrador encontrado"),
            @ApiResponse(responseCode = "404", description = "Administrador não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        Administrador admin = administradorService.buscarPorId(id);
        return ResponseEntity.ok(administradorMapper.toAdministradorResponseDTO(admin));
    }

    @Operation(summary = "Cadastrar um novo administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Administrador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já existente")
    })
    @PostMapping
    public ResponseEntity<AdministradorResponseDTO> cadastrar(@Valid @RequestBody AdministradorRequestDTO adminRequestDTO) {
        // Service now takes DTO, handles validation (email uniqueness), mapping, and hashing (TODO)
        // Throws RegraNegocioException
        Administrador novoAdmin = administradorService.cadastrar(adminRequestDTO);
        AdministradorResponseDTO dto = administradorMapper.toAdministradorResponseDTO(novoAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Atualizar um administrador existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Administrador atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Administrador não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já existente")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdministradorResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody AdministradorRequestDTO adminRequestDTO) {
         // Service now takes DTO, handles finding, validation, mapping, and saving
         // Throws RecursoNaoEncontradoException or RegraNegocioException
         Administrador adminAtualizado = administradorService.atualizar(id, adminRequestDTO);
         AdministradorResponseDTO dto = administradorMapper.toAdministradorResponseDTO(adminAtualizado);
         return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Excluir um administrador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Administrador excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Administrador não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        administradorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
