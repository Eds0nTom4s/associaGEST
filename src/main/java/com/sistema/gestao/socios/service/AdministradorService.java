package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.dto.AdministradorRequestDTO; // Import DTO
import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.mapper.AdministradorMapper; // Import Mapper
import com.sistema.gestao.socios.model.Administrador;
import com.sistema.gestao.socios.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
// TODO: Import PasswordEncoder if implementing hashing
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired // Inject Mapper
    private AdministradorMapper administradorMapper;

    // TODO: Inject PasswordEncoder for hashing
    // @Autowired
    // private PasswordEncoder passwordEncoder;
    @Transactional
    public Administrador cadastrar(AdministradorRequestDTO dto) {
        // Validation: Check if email already exists
        administradorRepository.findByEmail(dto.getEmail()).ifPresent(a -> {
            throw new RegraNegocioException("Email de administrador já cadastrado: " + dto.getEmail());
        });

        Administrador administrador = administradorMapper.toAdministrador(dto);
        // TODO: Hash password before saving
        // administrador.setSenha(passwordEncoder.encode(dto.getSenha()));

        return administradorRepository.save(administrador);
    }

    @Transactional(readOnly = true)
    public List<Administrador> listarTodos() {
        return administradorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Administrador buscarPorId(Long id) {
        return administradorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado com id: " + id));
    }

    @Transactional(readOnly = true)
    public Administrador buscarPorEmail(String email) {
         return administradorRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Administrador não encontrado com email: " + email));
    }

    @Transactional
    public Administrador atualizar(Long id, AdministradorRequestDTO dto) {
        Administrador adminExistente = buscarPorId(id); // Throws if not found

        // Validate email uniqueness if changed
        if (!adminExistente.getEmail().equalsIgnoreCase(dto.getEmail())) {
            administradorRepository.findByEmail(dto.getEmail()).ifPresent(a -> {
                if (!a.getId().equals(id)) { // Ensure it's not the same admin
                    throw new RegraNegocioException("Email já cadastrado para outro administrador: " + dto.getEmail());
                }
            });
            adminExistente.setEmail(dto.getEmail());
        }

        // Use mapper to update other fields (nome)
        administradorMapper.updateAdministradorFromDto(dto, adminExistente);

        // Password update should be handled separately
        // if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
        //     adminExistente.setSenha(passwordEncoder.encode(dto.getSenha()));
        // }

        return administradorRepository.save(adminExistente);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id); // Check if exists first
        // TODO: Add validation (e.g., cannot delete the last admin?)
        // if (administradorRepository.count() <= 1) {
        //     throw new RegraNegocioException("Não é possível excluir o último administrador.");
        // }
        administradorRepository.deleteById(id);
    }
}
