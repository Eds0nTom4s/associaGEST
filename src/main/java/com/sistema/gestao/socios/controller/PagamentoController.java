package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.PagamentoRequestDTO;
import com.sistema.gestao.socios.dto.PagamentoResponseDTO;
import com.sistema.gestao.socios.mapper.PagamentoMapper;
import com.sistema.gestao.socios.model.Categoria; // Needed for association
import com.sistema.gestao.socios.model.Pagamento;
import com.sistema.gestao.socios.model.Socio; // Needed for association
import com.sistema.gestao.socios.service.CategoriaService; // Needed for association
import com.sistema.gestao.socios.service.PagamentoService;
import com.sistema.gestao.socios.service.SocioService; // Needed for association
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// ResponseStatusException can be removed as GlobalExceptionHandler handles custom exceptions
// import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
// Optional is no longer returned by the refactored service method
// import java.util.Optional;

@RestController
@RequestMapping("/api/pagamentos")
@Tag(name = "Pagamentos", description = "API para gerenciamento de pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private SocioService socioService; // To fetch Socio

    @Autowired
    private CategoriaService categoriaService; // To fetch Categoria

    @Autowired
    private PagamentoMapper pagamentoMapper;

    @Operation(summary = "Registrar um novo pagamento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pagamento registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou sócio/categoria não encontrado")
    })
    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> registrarPagamento(@Valid @RequestBody PagamentoRequestDTO pagamentoRequestDTO) {
        // Service now takes DTO, fetches entities, handles validation
        // Throws RecursoNaoEncontradoException or RegraNegocioException
        Pagamento novoPagamento = pagamentoService.registrarPagamento(pagamentoRequestDTO);
        PagamentoResponseDTO dto = pagamentoMapper.toPagamentoResponseDTO(novoPagamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Listar todos os pagamentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listarTodos() {
        List<Pagamento> pagamentos = pagamentoService.listarTodos();
        List<PagamentoResponseDTO> dtos = pagamentoMapper.toPagamentoResponseDTOList(pagamentos);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        Pagamento pagamento = pagamentoService.buscarPorId(id);
        return ResponseEntity.ok(pagamentoMapper.toPagamentoResponseDTO(pagamento));
    }

    @Operation(summary = "Listar pagamentos por ID do sócio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos encontrados")
    })
    @GetMapping("/socio/{socioId}")
    public ResponseEntity<List<PagamentoResponseDTO>> buscarPorSocioId(@PathVariable Long socioId) {
        List<Pagamento> pagamentos = pagamentoService.buscarPorSocioId(socioId);
        List<PagamentoResponseDTO> dtos = pagamentoMapper.toPagamentoResponseDTOList(pagamentos);
        return ResponseEntity.ok(dtos);
    }

     @Operation(summary = "Listar pagamentos por ID da categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos encontrados")
    })
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PagamentoResponseDTO>> buscarPorCategoriaId(@PathVariable Long categoriaId) {
        List<Pagamento> pagamentos = pagamentoService.buscarPorCategoriaId(categoriaId);
        List<PagamentoResponseDTO> dtos = pagamentoMapper.toPagamentoResponseDTOList(pagamentos);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar pagamentos por status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamentos encontrados")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PagamentoResponseDTO>> buscarPorStatus(@PathVariable String status) {
        List<Pagamento> pagamentos = pagamentoService.buscarPorStatus(status);
        List<PagamentoResponseDTO> dtos = pagamentoMapper.toPagamentoResponseDTOList(pagamentos);
        return ResponseEntity.ok(dtos);
    }

     @Operation(summary = "Listar pagamentos por período")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Pagamentos encontrados"),
             @ApiResponse(responseCode = "400", description = "Datas inválidas")
     })
     @GetMapping("/periodo")
     public ResponseEntity<List<PagamentoResponseDTO>> buscarPorPeriodo(
             @Parameter(description = "Data de início (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date inicio,
             @Parameter(description = "Data de fim (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fim) {
         // Service now validates dates and throws RegraNegocioException if invalid
         List<Pagamento> pagamentos = pagamentoService.buscarPorPeriodo(inicio, fim);
         List<PagamentoResponseDTO> dtos = pagamentoMapper.toPagamentoResponseDTOList(pagamentos);
         return ResponseEntity.ok(dtos);
     }

     @Operation(summary = "Atualizar status de um pagamento")
     @ApiResponses(value = {
             @ApiResponse(responseCode = "200", description = "Status do pagamento atualizado"),
             @ApiResponse(responseCode = "404", description = "Pagamento não encontrado"),
             @ApiResponse(responseCode = "400", description = "Status inválido")
     })
     @PutMapping("/{id}/status")
     public ResponseEntity<PagamentoResponseDTO> atualizarStatus(
             @PathVariable Long id,
             @Parameter(description = "Novo status do pagamento", required = true) @RequestParam String novoStatus) {
         // Service throws RecursoNaoEncontradoException or RegraNegocioException
         Pagamento pagamentoAtualizado = pagamentoService.atualizarStatus(id, novoStatus);
         PagamentoResponseDTO dto = pagamentoMapper.toPagamentoResponseDTO(pagamentoAtualizado);
         return ResponseEntity.ok(dto);
     }

    // DELETE endpoint for payments is generally not recommended unless specific business rules allow it.
}
