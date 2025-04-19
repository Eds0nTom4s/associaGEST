package com.sistema.gestao.socios.repository;

import com.sistema.gestao.socios.model.RelatorioFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface RelatorioFinanceiroRepository extends JpaRepository<RelatorioFinanceiro, Long> {
    List<RelatorioFinanceiro> findByTipoRelatorio(String tipoRelatorio);
    List<RelatorioFinanceiro> findByPeriodoInicioBetween(Date startDate, Date endDate);
}
