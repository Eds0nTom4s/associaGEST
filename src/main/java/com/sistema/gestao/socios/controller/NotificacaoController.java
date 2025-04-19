package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.NotificacaoRequestDTO;
import com.sistema.gestao.socios.dto.NotificacaoResponseDTO;
import com.sistema.gestao.socios.mapper.NotificacaoMapper;
import com.sistema.gestao.socios.model.Notificacao;
import com.sistema.gestao.socios.model.Socio; // Needed for association
import com.sistema.gestao.socios.service.NotificacaoService;
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
@RequestMapping("/api/notificacoes")
@Tag(name = "Notificações", description = "API para gerenciamento e envio de notificações")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private SocioService socioService; // To fetch Socio

    @Autowired
    private NotificacaoMapper notificacaoMapper;

    @Operation(summary = "Criar (e potencialmente enviar) uma nova notificação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notificação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou sócio não encontrado")
    })
    @PostMapping
    public ResponseEntity<NotificacaoResponseDTO> criarNotificacao(@Valid @RequestBody NotificacaoRequestDTO notificacaoRequestDTO) {
        // Service now takes DTO, fetches Socio, handles validation
        // Throws RecursoNaoEncontradoException or RegraNegocioException
        Notificacao novaNotificacao = notificacaoService.criarNotificacao(notificacaoRequestDTO);
        NotificacaoResponseDTO dto = notificacaoMapper.toNotificacaoResponseDTO(novaNotificacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Listar todas as notificações")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações listadas com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listarTodas() {
        List<Notificacao> notificacoes = notificacaoService.listarTodas();
        List<NotificacaoResponseDTO> dtos = notificacaoMapper.toNotificacaoResponseDTOList(notificacoes);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar notificação por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificação encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        Notificacao notificacao = notificacaoService.buscarPorId(id);
        return ResponseEntity.ok(notificacaoMapper.toNotificacaoResponseDTO(notificacao));
    }

    @Operation(summary = "Listar notificações por ID do sócio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações encontradas")
    })
    @GetMapping("/socio/{socioId}")
    public ResponseEntity<List<NotificacaoResponseDTO>> buscarPorSocioId(@PathVariable Long socioId) {
        // Optional: Check if socio exists first
        // socioService.buscarPorId(socioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sócio não encontrado com ID: " + socioId));
        List<Notificacao> notificacoes = notificacaoService.buscarPorSocioId(socioId);
        List<NotificacaoResponseDTO> dtos = notificacaoMapper.toNotificacaoResponseDTOList(notificacoes);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar notificações por tipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações encontradas")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<NotificacaoResponseDTO>> buscarPorTipo(@PathVariable String tipo) {
        List<Notificacao> notificacoes = notificacaoService.buscarPorTipo(tipo);
        List<NotificacaoResponseDTO> dtos = notificacaoMapper.toNotificacaoResponseDTOList(notificacoes);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar notificações por período de envio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações encontradas"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas")
    })
    @GetMapping("/periodo")
    public ResponseEntity<List<NotificacaoResponseDTO>> buscarPorPeriodo(
             @Parameter(description = "Data de início (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date inicio,
             @Parameter(description = "Data de fim (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fim) {
         // Service now validates dates and throws RegraNegocioException if invalid
         List<Notificacao> notificacoes = notificacaoService.buscarPorPeriodo(inicio, fim);
         List<NotificacaoResponseDTO> dtos = notificacaoMapper.toNotificacaoResponseDTOList(notificacoes);
         return ResponseEntity.ok(dtos);
    }

    // DELETE/PUT for notifications might not be standard use cases.
}
