package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.NotificacaoRequestDTO;
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.NotificacaoMapper;
import com.sistema.gestao.socios.model.Notificacao;
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.NotificacaoRepository;
import com.sistema.gestao.socios.repository.SocioRepository;
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
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private SocioRepository socioRepository; // Mock SocioRepository

    @Mock
    private NotificacaoMapper notificacaoMapper;

    @InjectMocks
    private NotificacaoService notificacaoService;

    private Notificacao notificacao;
    private Socio socio;
    private NotificacaoRequestDTO notificacaoRequestDTO;

    @BeforeEach
    void setUp() {
        socio = new Socio(1L, "Carlos", "11122233344", "carlos@example.com", "222", "pass", "ATIVO", null, null, null);
        notificacao = new Notificacao(1L, "AVISO", new Date(), "Mensagem de teste", socio);

        notificacaoRequestDTO = new NotificacaoRequestDTO();
        notificacaoRequestDTO.setTipoNotificacao("AVISO");
        notificacaoRequestDTO.setMensagem("Mensagem de teste");
        notificacaoRequestDTO.setSocioId(1L);
    }

    @Test
    void testCriarNotificacao_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(notificacaoMapper.toNotificacao(any(NotificacaoRequestDTO.class))).thenReturn(notificacao); // Assume mapper returns base entity
        when(notificacaoRepository.save(any(Notificacao.class))).thenReturn(notificacao);

        Notificacao result = notificacaoService.criarNotificacao(notificacaoRequestDTO);

        assertNotNull(result);
        assertEquals(socio, result.getSocio());
        assertNotNull(result.getDataEnvio()); // Check date is set
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoMapper, times(1)).toNotificacao(notificacaoRequestDTO);
        verify(notificacaoRepository, times(1)).save(notificacao);
        // TODO: Verify actual sending mechanism if implemented
    }

    @Test
    void testCriarNotificacao_Fail_SocioNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            notificacaoService.criarNotificacao(notificacaoRequestDTO);
        });

        assertEquals("Sócio não encontrado com id: 1", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

     @Test
    void testEnviarNotificacaoParaSocio_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));
        when(notificacaoRepository.save(any(Notificacao.class))).thenAnswer(invocation -> {
             Notificacao saved = invocation.getArgument(0);
             saved.setId(2L); // Simulate saving and getting an ID
             return saved;
        });

        Notificacao result = notificacaoService.enviarNotificacaoParaSocio(1L, "PAGAMENTO", "Seu pagamento está pendente.");

        assertNotNull(result);
        assertEquals(socio, result.getSocio());
        assertEquals("PAGAMENTO", result.getTipoNotificacao());
        assertEquals("Seu pagamento está pendente.", result.getMensagem());
        assertNotNull(result.getDataEnvio());
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoRepository, times(1)).save(any(Notificacao.class));
         // TODO: Verify actual sending mechanism if implemented
    }

     @Test
    void testEnviarNotificacaoParaSocio_Fail_SocioNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            notificacaoService.enviarNotificacaoParaSocio(99L, "TESTE", "Msg");
        });

        assertEquals("Sócio não encontrado para enviar notificação: 99", exception.getMessage());
        verify(socioRepository, times(1)).findById(99L);
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

     @Test
    void testEnviarNotificacaoParaSocio_Fail_TipoNull() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            notificacaoService.enviarNotificacaoParaSocio(1L, null, "Msg");
        });

        assertEquals("Tipo da notificação não pode ser vazio.", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }

     @Test
    void testEnviarNotificacaoParaSocio_Fail_MensagemNull() {
         when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio));

        RegraNegocioException exception = assertThrows(RegraNegocioException.class, () -> {
            notificacaoService.enviarNotificacaoParaSocio(1L, "TIPO", null);
        });

        assertEquals("Mensagem da notificação não pode ser vazia.", exception.getMessage());
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoRepository, never()).save(any(Notificacao.class));
    }


    @Test
    void testListarTodas() {
        when(notificacaoRepository.findAll()).thenReturn(List.of(notificacao));
        List<Notificacao> result = notificacaoService.listarTodas();
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(notificacaoRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorId_Success() {
        when(notificacaoRepository.findById(anyLong())).thenReturn(Optional.of(notificacao));
        Notificacao result = notificacaoService.buscarPorId(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(notificacaoRepository, times(1)).findById(1L);
    }

    @Test
    void testBuscarPorId_Fail_NotFound() {
        when(notificacaoRepository.findById(anyLong())).thenReturn(Optional.empty());
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            notificacaoService.buscarPorId(1L);
        });
        assertEquals("Notificação não encontrada com id: 1", exception.getMessage());
        verify(notificacaoRepository, times(1)).findById(1L);
    }

     @Test
    void testBuscarPorSocioId_Success() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.of(socio)); // Ensure socio exists
        when(notificacaoRepository.findBySocioId(anyLong())).thenReturn(List.of(notificacao));

        List<Notificacao> result = notificacaoService.buscarPorSocioId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSocio().getId());
        verify(socioRepository, times(1)).findById(1L);
        verify(notificacaoRepository, times(1)).findBySocioId(1L);
    }

     @Test
    void testBuscarPorSocioId_Fail_SocioNotFound() {
        when(socioRepository.findById(anyLong())).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class, () -> {
            notificacaoService.buscarPorSocioId(99L); // Non-existent socio ID
        });

        assertEquals("Sócio não encontrado com id: 99", exception.getMessage());
        verify(socioRepository, times(1)).findById(99L);
        verify(notificacaoRepository, never()).findBySocioId(anyLong());
    }

    // Similar tests for buscarPorTipo and buscarPorPeriodo

    // Test for deletar if implemented
}
