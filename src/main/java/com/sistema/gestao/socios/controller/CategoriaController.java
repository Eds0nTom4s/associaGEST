package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.CategoriaRequestDTO;
import com.sistema.gestao.socios.dto.CategoriaResponseDTO;
import com.sistema.gestao.socios.mapper.CategoriaMapper;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// ResponseStatusException can be removed if not used directly anymore
// import org.springframework.web.server.ResponseStatusException;

import java.util.List;
// Optional is no longer returned by the refactored service method
// import java.util.Optional;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "API para gerenciamento de categorias de sócios")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaMapper categoriaMapper;

    @Operation(summary = "Listar todas as categorias")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodos() {
        List<Categoria> categorias = categoriaService.listarTodos();
        List<CategoriaResponseDTO> dtos = categoriaMapper.toCategoriaResponseDTOList(categorias);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar categoria por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service now throws RecursoNaoEncontradoException if not found, handled by GlobalExceptionHandler
        Categoria categoria = categoriaService.buscarPorId(id);
        CategoriaResponseDTO dto = categoriaMapper.toCategoriaResponseDTO(categoria);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Cadastrar uma nova categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> cadastrar(@Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        // Service now throws RegraNegocioException if name exists, handled by GlobalExceptionHandler
        Categoria categoria = categoriaMapper.toCategoria(categoriaRequestDTO);
        Categoria novaCategoria = categoriaService.cadastrar(categoria);
        CategoriaResponseDTO dto = categoriaMapper.toCategoriaResponseDTO(novaCategoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Atualizar uma categoria existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        // Service now handles finding, validation (name uniqueness), mapping, and saving
        // It throws RecursoNaoEncontradoException or RegraNegocioException, handled by GlobalExceptionHandler
        Categoria categoriaAtualizada = categoriaService.atualizar(id, categoriaRequestDTO);
        CategoriaResponseDTO dto = categoriaMapper.toCategoriaResponseDTO(categoriaAtualizada);
        return ResponseEntity.ok(dto);
    }


    @Operation(summary = "Excluir uma categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Service now throws RecursoNaoEncontradoException or RegraNegocioException, handled by GlobalExceptionHandler
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
