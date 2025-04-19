package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.PagamentoRequestDTO; // Import DTO
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.PagamentoMapper; // Import Mapper
import com.sistema.gestao.socios.model.Categoria; // Import Categoria
import com.sistema.gestao.socios.model.Pagamento;
import com.sistema.gestao.socios.model.Socio; // Import Socio
import com.sistema.gestao.socios.repository.PagamentoRepository;
import com.sistema.gestao.socios.repository.SocioRepository; // Import SocioRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private SocioRepository socioRepository;

    @Autowired // Inject CategoriaService for validation
    private CategoriaService categoriaService;

    @Autowired // Inject Mapper
    private PagamentoMapper pagamentoMapper;
    @Transactional
    public Pagamento registrarPagamento(PagamentoRequestDTO dto) {
        // Validate Socio and Categoria existence
        Socio socio = socioRepository.findById(dto.getSocioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com id: " + dto.getSocioId())); // Standardizing to lowercase 'id'
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId()); // Throws if not found

        // Example validation: Only allow payment for socios with specific status
        // if (!"ATIVO".equalsIgnoreCase(socio.getStatusPagamento()) && !"PENDENTE".equalsIgnoreCase(socio.getStatusPagamento())) {
        //     throw new RegraNegocioException("Pagamento não permitido para sócio com status: " + socio.getStatusPagamento());
        // }

        Pagamento pagamento = pagamentoMapper.toPagamento(dto);
        pagamento.setSocio(socio);
        pagamento.setCategoria(categoria);
        pagamento.setDataPagamento(new Date()); // Set payment date on registration

        // TODO: Potentially update Socio's statusPagamento based on this payment (e.g., if status is CONFIRMADO)
        // if ("CONFIRMADO".equalsIgnoreCase(dto.getStatus())) {
        //     socio.setStatusPagamento("PAGO"); // Or logic based on payment value/date
        //     socioRepository.save(socio);
        // }

        return pagamentoRepository.save(pagamento);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> listarTodos() {
        return pagamentoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pagamento buscarPorId(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pagamento não encontrado com id: " + id)); // Already lowercase 'id'
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPorSocioId(Long socioId) {
        // Optional: Check if socio exists first
        socioRepository.findById(socioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com id: " + socioId)); // Already lowercase 'id'
        return pagamentoRepository.findBySocioId(socioId);
    }

     @Transactional(readOnly = true)
    public List<Pagamento> buscarPorCategoriaId(Long categoriaId) {
        // Optional: Check if categoria exists first
        categoriaService.buscarPorId(categoriaId); // Throws if not found
        return pagamentoRepository.findByCategoriaId(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Pagamento> buscarPorPeriodo(Date inicio, Date fim) {
        if (inicio == null || fim == null || inicio.after(fim)) {
            throw new RegraNegocioException("Período de datas inválido.");
        }
        return pagamentoRepository.findByDataPagamentoBetween(inicio, fim);
    }

     @Transactional(readOnly = true)
    public List<Pagamento> buscarPorStatus(String status) {
        // TODO: Validate status string if necessary (e.g., enum)
        return pagamentoRepository.findByStatus(status);
    }

    // Update and Delete methods might be less common for payments.

    // Example: Update payment status
    @Transactional
    public Pagamento atualizarStatus(Long id, String novoStatus) {
         // TODO: Validate novoStatus (e.g., against allowed values)
         if (novoStatus == null || novoStatus.trim().isEmpty()) {
             throw new RegraNegocioException("Novo status não pode ser vazio.");
         }

         Pagamento pagamentoExistente = buscarPorId(id); // Throws if not found

         // TODO: Add validation for allowed status transitions if needed
         // Ex: if ("CONFIRMADO".equals(pagamentoExistente.getStatus())) throw new RegraNegocioException("Pagamento já confirmado não pode ser alterado.");

         pagamentoExistente.setStatus(novoStatus);

         // TODO: Potentially update Socio's statusPagamento based on this status change
         // if ("CONFIRMADO".equalsIgnoreCase(novoStatus)) {
         //     Socio socio = pagamentoExistente.getSocio();
         //     socio.setStatusPagamento("PAGO"); // Or more complex logic
         //     socioRepository.save(socio);
         // }

         return pagamentoRepository.save(pagamentoExistente);
    }

    // Deleting payments might require specific business rules (e.g., only if status is PENDING)
    // @Transactional
    // public void deletar(Long id) {
    //     Pagamento pagamento = buscarPorId(id); // Check if exists
    //     // TODO: Add validation before deleting (e.g., check status)
    //     // if (!"PENDENTE".equals(pagamento.getStatus())) {
    //     //     throw new RegraNegocioException("Só é possível excluir pagamentos com status PENDENTE.");
    //     // }
    //     pagamentoRepository.deleteById(id);
    // }
}
