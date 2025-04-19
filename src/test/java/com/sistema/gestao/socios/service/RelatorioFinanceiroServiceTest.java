package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.RelatorioFinanceiroMapper;
import com.sistema.gestao.socios.model.RelatorioFinanceiro;
import com.sistema.gestao.socios.repository.PagamentoRepository; // Example dependency
import com.sistema.gestao.socios.repository.RelatorioFinanceiroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioFinanceiroServiceTest {

    @Mock
    private RelatorioFinanceiroRepository relatorioRepository;

    @Mock
    private PagamentoRepository pagamentoRepository; // Mock dependency for generation logic

    @Mock
    private RelatorioFinanceiroMapper relatorioMapper; // Although not used in current service logic

    @InjectMocks
    private RelatorioFinanceiroService relatorioService;

    private RelatorioFinanceiro relatorio;
    private Date inicio;
    private Date fim;

    @BeforeEach
    void setUp() {
        inicio = new Date(System.currentTimeMillis() - 100000);
        fim = new Date();
        relatorio = new RelatorioFinanceiro(1L, "MENSAL", inicio, fim, "Dados...");
    }

    @Test
    void testGerarRelatorio_Success() {
        when(relatorioRepository.save(any(RelatorioFinanceiro.class))).thenReturn(relatorio);
        // Mock pagamentoRepository if generation logic uses it
        // when(pagamentoRepository.findByDataPagamentoBetween(any(), any())).thenReturn(Collections.emptyList());

        RelatorioFinanceiro result = relatorioService.gerarRelatorio("MENSAL", inicio, fim);

        assertNotNull(result);
        assertEquals("MENSAL", result.getTipoRelatorio());
        assertEquals(inicio, result.getPeriodoInicio());
        assertEquals(fim, result.getPeriodoFim());
        assertNotNull(result.getDadosRelatorio());
        verify(relatorioRepository, times(1)).save(any(RelatorioFinanceiro.class));
        // Verify pagamentoRepository interactions if implemented
    }

    @Test
    void testGerarRelatorio_Fail_TipoNull() {
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            relatorioService.gerarRelatorio(null, inicio, fim);
        });
        assertEquals("Tipo do relatório não pode ser vazio.", exception.getMessage());
        verify(relatorioRepository, never()).save(any(RelatorioFinanceiro.class));
    }

     @Test
    void testGerarRelatorio_Fail_InvalidDates() {
        Date invalidFim = new Date(inicio.getTime() - 1000); // End before start
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            relatorioService.gerarRelatorio("MENSAL", inicio, invalidFim);
        });
        assertEquals("Período de datas inválido para o relatório.", exception.getMessage());
        verify(relatorioRepository, never()).save(any(RelatorioFinanceiro.class));
    }

    @Test
    void testListarTodos() {
        when(relatorioRepository.findAll()).thenReturn(List.of(relatorio));
        List<RelatorioFinanceiro> result = relatorioService.listarTodos();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(relatorioRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(relatorioRepository.findById(anyLong())).thenReturn(Optional.of(relatorio));
        RelatorioFinanceiro result = relatorioService.buscarPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(relatorioRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(relatorioRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            relatorioService.buscarPorId(1L);
        });
        assertEquals("Relatório não encontrado com id: 1", exception.getMessage());
        verify(relatorioRepository, times(1)).findById(1L);
    }

    // Similar tests for buscarPorTipo and buscarPorPeriodo

    @Test
    void testDeletar_Success() {
        when(relatorioRepository.findById(anyLong())).thenReturn(Optional.of(relatorio));
        assertDoesNotThrow(() -> relatorioService.deletar(1L));
        verify(relatorioRepository, times(1)).findById(1L);
        verify(relatorioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletar_Fail_NotFound() {
        when(relatorioRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            relatorioService.deletar(1L);
        });
        assertEquals("Relatório não encontrado com id: 1", exception.getMessage());
        verify(relatorioRepository, times(1)).findById(1L);
        verify(relatorioRepository, never()).deleteById(anyLong());
    }
}
