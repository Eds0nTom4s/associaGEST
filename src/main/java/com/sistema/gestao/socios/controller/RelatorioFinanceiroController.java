package com.sistema.gestao.socios.controller;

import com.sistema.gestao.socios.dto.RelatorioFinanceiroRequestDTO;
import com.sistema.gestao.socios.dto.RelatorioFinanceiroResponseDTO;
import com.sistema.gestao.socios.mapper.RelatorioFinanceiroMapper;
import com.sistema.gestao.socios.model.RelatorioFinanceiro;
import com.sistema.gestao.socios.service.RelatorioFinanceiroService;
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
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios Financeiros", description = "API para geração e consulta de relatórios financeiros")
public class RelatorioFinanceiroController {

    @Autowired
    private RelatorioFinanceiroService relatorioService;

    @Autowired
    private RelatorioFinanceiroMapper relatorioMapper;

    @Operation(summary = "Gerar um novo relatório financeiro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    @PostMapping
    public ResponseEntity<RelatorioFinanceiroResponseDTO> gerarRelatorio(@Valid @RequestBody RelatorioFinanceiroRequestDTO requestDTO) {
        // Service now handles validation and generation
        // Throws RegraNegocioException
        RelatorioFinanceiro novoRelatorio = relatorioService.gerarRelatorio(
                requestDTO.getTipoRelatorio(),
                requestDTO.getPeriodoInicio(),
                requestDTO.getPeriodoFim()
        );
        RelatorioFinanceiroResponseDTO dto = relatorioMapper.toRelatorioFinanceiroResponseDTO(novoRelatorio);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Listar todos os relatórios financeiros gerados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<RelatorioFinanceiroResponseDTO>> listarTodos() {
        List<RelatorioFinanceiro> relatorios = relatorioService.listarTodos();
        List<RelatorioFinanceiroResponseDTO> dtos = relatorioMapper.toRelatorioFinanceiroResponseDTOList(relatorios);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Buscar relatório financeiro por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório encontrado"),
            @ApiResponse(responseCode = "404", description = "Relatório não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<RelatorioFinanceiroResponseDTO> buscarPorId(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        RelatorioFinanceiro relatorio = relatorioService.buscarPorId(id);
        return ResponseEntity.ok(relatorioMapper.toRelatorioFinanceiroResponseDTO(relatorio));
    }

    @Operation(summary = "Listar relatórios financeiros por tipo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios encontrados")
    })
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<RelatorioFinanceiroResponseDTO>> buscarPorTipo(@PathVariable String tipo) {
        List<RelatorioFinanceiro> relatorios = relatorioService.buscarPorTipo(tipo);
        List<RelatorioFinanceiroResponseDTO> dtos = relatorioMapper.toRelatorioFinanceiroResponseDTOList(relatorios);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Listar relatórios financeiros por período de geração")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios encontrados"),
            @ApiResponse(responseCode = "400", description = "Datas inválidas")
    })
    @GetMapping("/periodo")
    public ResponseEntity<List<RelatorioFinanceiroResponseDTO>> buscarPorPeriodo(
             @Parameter(description = "Data de início do período do relatório (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date inicio,
             @Parameter(description = "Data de fim do período do relatório (yyyy-MM-dd)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fim) {
         // Service now validates dates and throws RegraNegocioException if invalid
         List<RelatorioFinanceiro> relatorios = relatorioService.buscarPorPeriodo(inicio, fim);
         List<RelatorioFinanceiroResponseDTO> dtos = relatorioMapper.toRelatorioFinanceiroResponseDTOList(relatorios);
         return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Excluir um relatório financeiro gerado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        // Service throws RecursoNaoEncontradoException if not found
        relatorioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}