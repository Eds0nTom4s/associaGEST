package com.sistema.gestao.socios.repository;

import com.sistema.gestao.socios.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Date;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findBySocioId(Long socioId);
    List<Pagamento> findByCategoriaId(Long categoriaId);
    List<Pagamento> findByDataPagamentoBetween(Date startDate, Date endDate);
    List<Pagamento> findByStatus(String status);
}
