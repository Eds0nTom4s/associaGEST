package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.SocioRequestDTO;
import com.sistema.gestao.socios.dto.SocioResponseDTO;
import com.sistema.gestao.socios.mapper.SocioMapper;
import com.sistema.gestao.socios.model.Categoria; // Needed for association
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.service.CategoriaService; // Needed for association
import com.sistema.gestao.socios.service.SocioService;
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
@RequestMapping("/api/socios")
@Tag(name = "Sócios", description = "API para gerenciamento de sócios")
public class SocioController {

    @Autowired
    private SocioService socioService;

    @Autowired
    private CategoriaService categoriaService; // Inject CategoriaService to fetch Categoria

    @Autowired
    private SocioMapper socioMapper;

    @Operation(summary = "Listar todos os sócios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sócios listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<SocioResponseDTO>> listarTodos() {
        List<Socio> socios = socioService.listarTodos();
        List<SocioResponseDTO> dtos = socioMapper.toSocioResponseDTOList(socios);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar sócio por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sócio encontrado"),
            @ApiResponse(responseCode = "404", description = "Sócio não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        Socio socio = socioService.buscarPorId(id);
        return ResponseEntity.ok(socioMapper.toSocioResponseDTO(socio));
    }

    @Operation(summary = "Cadastrar um novo sócio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sócio cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou categoria não encontrada")
    })
    @PostMapping
    public ResponseEntity<SocioResponseDTO> cadastrar(@Valid @RequestBody SocioRequestDTO socioRequestDTO) {
        // Service now takes DTO, fetches Categoria, handles validation (duplicates), and hashing (TODO)
        // Throws RecursoNaoEncontradoException or RegraNegocioException
        Socio novoSocio = socioService.cadastrar(socioRequestDTO);
        SocioResponseDTO dto = socioMapper.toSocioResponseDTO(novoSocio);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Atualizar um sócio existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sócio atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sócio não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou categoria não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody SocioRequestDTO socioRequestDTO) {
        // Service now takes DTO, handles finding, validation, mapping, and saving
        // Throws RecursoNaoEncontradoException or RegraNegocioException
        Socio socioAtualizado = socioService.atualizar(id, socioRequestDTO);
        SocioResponseDTO dto = socioMapper.toSocioResponseDTO(socioAtualizado);
        return ResponseEntity.ok(dto);
    }


    @Operation(summary = "Excluir um sócio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Sócio excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Sócio não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        socioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
