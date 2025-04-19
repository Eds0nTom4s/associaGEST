package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.PagamentoRequestDTO;
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.PagamentoMapper;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.model.Pagamento;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.PagamentoRepository;
import com.sistema.gestao.socios.repository.SocioRepository; // Needed for validation
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private SocioRepository socioRepository; // Mock SocioRepository

    @Mock
    private CategoriaService categoriaService; // Mock CategoriaService

    @Mock
    private PagamentoMapper pagamentoMapper;

    @InjectMocks
    private PagamentoService pagamentoService;

    private Pagamento pagamento;
    private Socio socio;
    private Categoria categoria;
    private PagamentoRequestDTO pagamentoRequestDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Standard", "Benefícios básicos", new BigDecimal("50.00"), null, null);
        socio = new Socio(1L, "Maria", "98765432100", "maria@example.com", "111", "pwd", "PENDENTE", categoria, null, null);
        pagamento = new Pagamento(1L, new Date(), new BigDecimal("50.00"), "CONFIRMADO", socio, categoria);

        pagamentoRequestDTO = new PagamentoRequestDTO();
        pagamentoRequestDTO.setValorPago(new BigDecimal("50.00"));
        pagamentoRequestDTO.setStatus("CONFIRMADO");
        pagamentoRequestDTO.setSocioId(1L);
        pagamentoRequestDTO.setCategoriaId(1L);
    }

    @Test
    void testRegistrarPagamento_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(categoriaService.buscarPorId(anyLong())).thenReturn(categoria);
        when(pagamentoMapper.toPagamento(any(PagamentoRequestDTO.class))).thenReturn(pagamento); // Assume mapper returns base entity
        when(pagamentoRepository.save(any(Pagamento.class))).thenReturn(pagamento);

        Pagamento result = pagamentoService.registrarPagamento(pagamentoRequestDTO);

        assertNotNull(result);
        assertEquals(socio, result.getSocio());
        assertEquals(categoria, result.getCategoria());
        assertNotNull(result.getDataPagamento()); // Check date is set
        verify(socioRepository, times(1)).findById(1L);
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(pagamentoMapper, times(1)).toPagamento(pagamentoRequestDTO);
        verify(pagamentoRepository, times(1)).save(pagamento);
        // TODO: Verify socio status update if implemented
    }

    @Test
    void testRegistrarPagamento_Fail_SocioNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());
        // No need to mock categoriaService as it won't be called

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            pagamentoService.registrarPagamento(pagamentoRequestDTO);
        });

        assertEquals("Sócio não encontrado com id: 1", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(categoriaService, never()).buscarPorId(anyLong());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

     @Test
    void testRegistrarPagamento_Fail_CategoriaNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(categoriaService.buscarPorId(anyLong())).thenThrow(new RecursoNaoEncontradoException("Categoria não encontrada"));

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            pagamentoService.registrarPagamento(pagamentoRequestDTO);
        });

        assertEquals("Categoria não encontrada", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(categoriaService, times(1)).buscarPorId(1L);
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

    // TODO: Add test for registrarPagamento_Fail_SocioInactive if that validation is added

    @Test
    void testListarTodos() {
        when(pagamentoRepository.findAll()).thenReturn(List.of(pagamento));
        List<Pagamento> result = pagamentoService.listarTodos();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pagamentoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.of(pagamento));
        Pagamento result = pagamentoService.buscarPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(pagamentoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            pagamentoService.buscarPorId(1L);
        });
        assertEquals("Pagamento não encontrado com id: 1", exception.getMessage());
        verify(pagamentoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorSocioId_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio)); // Ensure socio exists
        when(pagamentoRepository.findBySocioId(anyLong())).thenReturn(List.of(pagamento));

        List<Pagamento> result = pagamentoService.buscarPorSocioId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSocio().getId());
        verify(socioRepository, times(1)).findById(1L);
        verify(pagamentoRepository, times(1)).findBySocioId(1L);
    }

     @Test
    void testBuscarPorSocioId_Fail_SocioNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            pagamentoService.buscarPorSocioId(99L); // Non-existent socio ID
        });

        assertEquals("Sócio não encontrado com id: 99", exception.getMessage());
        verify(socioRepository, times(1)).findById(99L);
        verify(pagamentoRepository, never()).findBySocioId(anyLong());
    }

    // Similar tests for buscarPorCategoriaId

    @Test
    void testBuscarPorPeriodo_Success() {
        Date start = new Date();
        Date end = new Date(start.getTime() + 10000);
        when(pagamentoRepository.findByDataPagamentoBetween(any(Date.class), any(Date.class))).thenReturn(List.of(pagamento));

        List<Pagamento> result = pagamentoService.buscarPorPeriodo(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(pagamentoRepository, times(1)).findByDataPagamentoBetween(start, end);
    }

     @Test
    void testBuscarPorPeriodo_Fail_InvalidDates() {
        Date start = new Date();
        Date end = new Date(start.getTime() - 10000); // End before start

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            pagamentoService.buscarPorPeriodo(start, end);
        });

        assertEquals("Período de datas inválido.", exception.getMessage());
        verify(pagamentoRepository, never()).findByDataPagamentoBetween(any(Date.class), any(Date.class));
    }

    // Similar tests for buscarPorStatus

    @Test
    void testAtualizarStatus_Success() {
        String novoStatus = "REJEITADO";
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.of(pagamento));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pagamento result = pagamentoService.atualizarStatus(1L, novoStatus);

        assertNotNull(result);
        assertEquals(novoStatus, result.getStatus());
        verify(pagamentoRepository, times(1)).findById(1L);
        verify(pagamentoRepository, times(1)).save(pagamento);
        // TODO: Verify socio status update if implemented
    }

     @Test
    void testAtualizarStatus_Fail_NotFound() {
        when(pagamentoRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            pagamentoService.atualizarStatus(1L, "PENDENTE");
        });

        assertEquals("Pagamento não encontrado com id: 1", exception.getMessage());
        verify(pagamentoRepository, times(1)).findById(1L);
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

     @Test
    void testAtualizarStatus_Fail_StatusNull() {
        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            pagamentoService.atualizarStatus(1L, null);
        });
        assertEquals("Novo status não pode ser vazio.", exception.getMessage());
        verify(pagamentoRepository, never()).findById(anyLong());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

     @Test
    void testAtualizarStatus_Fail_StatusEmpty() {
         RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            pagamentoService.atualizarStatus(1L, "  ");
        });
        assertEquals("Novo status não pode ser vazio.", exception.getMessage());
        verify(pagamentoRepository, never()).findById(anyLong());
        verify(pagamentoRepository, never()).save(any(Pagamento.class));
    }

    // TODO: Add test for deletar if implemented
}
