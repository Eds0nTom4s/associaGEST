package com.sistema.gestao.socios.repository;

import com.sistema.gestao.socios.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findBySocioId(Long socioId);
    List<Notificacao> findByTipoNotificacao(String tipoNotificacao);
    List<Notificacao> findByDataEnvioBetween(Date startDate, Date endDate);
}
