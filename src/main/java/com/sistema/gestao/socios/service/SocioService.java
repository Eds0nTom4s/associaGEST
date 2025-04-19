package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.SocioRequestDTO; // Import DTO
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.SocioMapper; // Import Mapper
import com.sistema.gestao.socios.model.Categoria; // Import Categoria
import com.sistema.gestao.socios.model.Socio;
import com.sistema.gestao.socios.repository.SocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
// TODO: Import PasswordEncoder if implementing hashing
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SocioService {

    @Autowired
    private SocioRepository socioRepository;

    @Autowired // Inject Mapper
    private SocioMapper socioMapper;

    @Autowired // Inject CategoriaService for validation
    private CategoriaService categoriaService;

    // TODO: Inject PasswordEncoder for hashing
    // @Autowired
    // private PasswordEncoder passwordEncoder;
    @Transactional
    public Socio cadastrar(SocioRequestDTO dto) {
        // Validate if Categoria exists
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId()); // Throws if not found

        // Validation: Check if email or documento already exists
        socioRepository.findByEmail(dto.getEmail()).ifPresent(s -> {
            throw new RegraNegocioException("Email já cadastrado: " + dto.getEmail());
        });
        socioRepository.findByDocumento(dto.getDocumento()).ifPresent(s -> {
            throw new RegraNegocioException("Documento já cadastrado: " + dto.getDocumento());
        });

        Socio socio = socioMapper.toSocio(dto);
        socio.setCategoria(categoria);
        // TODO: Hash password before saving
        // socio.setSenha(passwordEncoder.encode(dto.getSenha()));
        socio.setStatusPagamento("PENDENTE"); // Example: Set initial status

        return socioRepository.save(socio);
    }

    @Transactional(readOnly = true)
    public List<Socio> listarTodos() {
        return socioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Socio buscarPorId(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com id: " + id)); // Corrected case
    }

     @Transactional(readOnly = true)
    public Socio buscarPorEmail(String email) {
        return socioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com email: " + email));
    }

     @Transactional(readOnly = true)
    public Socio buscarPorDocumento(String documento) {
        return socioRepository.findByDocumento(documento)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sócio não encontrado com documento: " + documento));
    }

    @Transactional
    public Socio atualizar(Long id, SocioRequestDTO dto) {
        Socio socioExistente = buscarPorId(id); // Throws if not found

        // Validate Categoria if changed
        Categoria categoria = socioExistente.getCategoria();
        if (!categoria.getId().equals(dto.getCategoriaId())) {
            categoria = categoriaService.buscarPorId(dto.getCategoriaId()); // Throws if not found
        }

        // Validate email uniqueness if changed
        if (!socioExistente.getEmail().equalsIgnoreCase(dto.getEmail())) {
            socioRepository.findByEmail(dto.getEmail()).ifPresent(s -> {
                if (!s.getId().equals(id)) { // Ensure it's not the same socio
                    throw new RegraNegocioException("Email já cadastrado para outro sócio: " + dto.getEmail());
                }
            });
            socioExistente.setEmail(dto.getEmail());
        }

        // Validate documento uniqueness if changed
        if (!socioExistente.getDocumento().equals(dto.getDocumento())) {
             socioRepository.findByDocumento(dto.getDocumento()).ifPresent(s -> {
                if (!s.getId().equals(id)) { // Ensure it's not the same socio
                    throw new RegraNegocioException("Documento já cadastrado para outro sócio: " + dto.getDocumento());
                }
            });
             socioExistente.setDocumento(dto.getDocumento());
        }

        // Use mapper to update other fields (name, telefone)
        socioMapper.updateSocioFromDto(dto, socioExistente);
        socioExistente.setCategoria(categoria); // Set potentially updated categoria

        // Password update should be handled separately, e.g., via a specific endpoint/service method
        // if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
        //     socioExistente.setSenha(passwordEncoder.encode(dto.getSenha()));
        // }

        return socioRepository.save(socioExistente);
    }

    @Transactional
    public void deletar(Long id) {
        Socio socio = buscarPorId(id); // Check if exists first

        // TODO: Add validation (e.g., check for related payments/notifications before deleting?)
        // Example: Check for payments
        // if (socio.getPagamentos() != null && !socio.getPagamentos().isEmpty()) {
        //    throw new RegraNegocioException("Não é possível excluir sócio pois existem pagamentos associados.");
        // }

        socioRepository.deleteById(id);
    }
}
