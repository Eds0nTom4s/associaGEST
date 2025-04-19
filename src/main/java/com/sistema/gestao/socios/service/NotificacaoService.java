package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.NotificacaoRequestDTO; // Import DTO
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.NotificacaoMapper; // Import Mapper
import com.sistema.gestao.socios.model.Notificacao;
import com.sistema.gestao.socios.model.Socio; // Import Socio
import com.sistema.gestao.socios.repository.NotificacaoRepository;
import com.sistema.gestao.socios.repository.SocioRepository; // Import SocioRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired // Inject Mapper
    private NotificacaoMapper notificacaoMapper;

    // TODO: Implement actual notification sending logic (email, SMS, etc.) - this service currently only manages records
    @Transactional
    public Notificacao criarNotificacao(NotificacaoRequestDTO dto) {
        // Validate Socio existence
        Socio socio = socioRepository.findById(dto.getSocioId())
             .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com id: " + dto.getSocioId()));

        Notificacao notificacao = notificacaoMapper.toNotificacao(dto);
        notificacao.setSocio(socio);
        notificacao.setDataEnvio(new Date()); // Set send date on creation

        // TODO: Trigger actual sending mechanism here based on tipoNotificacao

        return notificacaoRepository.save(notificacao);
    }

    // Convenience method to create and send notification (already uses custom exceptions indirectly)
    @Transactional
    public Notificacao enviarNotificacaoParaSocio(Long socioId, String tipo, String mensagem) {
         Socio socio = socioRepository.findById(socioId)
             .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado para enviar notificação: " + socioId));

         if (tipo == null || tipo.trim().isEmpty()) {
             throw new RegraNegocioException("Tipo da notificação não pode ser vazio.");
         }
          if (mensagem == null || mensagem.trim().isEmpty()) {
             throw new RegraNegocioException("Mensagem da notificação não pode ser vazia.");
         }

         Notificacao notificacao = new Notificacao();
         notificacao.setSocio(socio);
         notificacao.setTipoNotificacao(tipo);
         notificacao.setMensagem(mensagem);
         notificacao.setDataEnvio(new Date());

         // TODO: Trigger actual sending mechanism here

         return notificacaoRepository.save(notificacao);
    }


    @Transactional(readOnly = true)
    public List<Notificacao> listarTodas() {
        return notificacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Notificacao buscarPorId(Long id) {
        return notificacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Notificacao> buscarPorSocioId(Long socioId) {
        // Optional: Check if socio exists first
        socioRepository.findById(socioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com id: " + socioId));
        return notificacaoRepository.findBySocioId(socioId);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> buscarPorTipo(String tipo) {
        // TODO: Validate tipo if necessary
        return notificacaoRepository.findByTipoNotificacao(tipo);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> buscarPorPeriodo(Date inicio, Date fim) {
         if (inicio == null || fim == null || inicio.after(fim)) {
            throw new RegraNegocioException("Período de datas inválido.");
        }
        return notificacaoRepository.findByDataEnvioBetween(inicio, fim);
    }

    // Update/Delete for notifications might not be standard.
    // @Transactional
    // public void deletar(Long id) {
    //     buscarPorId(id); // Check if exists
    //     notificacaoRepository.deleteById(id);
    // }
}
