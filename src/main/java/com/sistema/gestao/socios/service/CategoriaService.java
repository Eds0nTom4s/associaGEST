package com.sistema.gestao.socios.service;

import com.sistema.gestao.socios.exception.RecursoNaoEncontradoException;
import com.sistema.gestao.socios.exception.RegraNegocioException;
import com.sistema.gestao.socios.model.Categoria;
import com.sistema.gestao.socios.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Collections; // Import Collections

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired // Inject Mapper
    private com.sistema.gestao.socios.mapper.CategoriaMapper categoriaMapper;

    @Transactional
    public Categoria cadastrar(Categoria categoria) {
        // Validation: Check if category name already exists (case-insensitive example)
        categoriaRepository.findByNomeIgnoreCase(categoria.getNome()).ifPresent(c -> {
            throw new RegraNegocioException("Nome da categoria já existe: " + categoria.getNome());
        });
        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodos() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));
    }

    @Transactional
    public Categoria atualizar(Long id, com.sistema.gestao.socios.dto.CategoriaRequestDTO dto) {
        Categoria categoriaExistente = buscarPorId(id); // Uses the method above which throws if not found

        // Check if name is being changed and if the new name already exists
        if (!categoriaExistente.getNome().equalsIgnoreCase(dto.getNome())) {
            categoriaRepository.findByNomeIgnoreCase(dto.getNome()).ifPresent(c -> {
                 throw new RegraNegocioException("Nome da categoria já existe: " + dto.getNome());
            });
        }

        // Use mapper to update fields from DTO
        categoriaMapper.updateCategoriaFromDto(dto, categoriaExistente);
        return categoriaRepository.save(categoriaExistente);
    }

    @Transactional
    public void deletar(Long id) {
        Categoria categoria = buscarPorId(id); // Check if exists first

        // Validation: Check if category is associated with any Socio
        // Ensure lazy-loaded collection is initialized within the transaction
        if (categoria.getSocios() != null && !categoria.getSocios().isEmpty()) {
             throw new RegraNegocioException("Não é possível excluir categoria pois existem sócios associados.");
        }
        // Add similar check for Pagamentos if needed
        // if (categoria.getPagamentos() != null && !categoria.getPagamentos().isEmpty()) {
        //     throw new RegraNegocioException("Não é possível excluir categoria pois existem pagamentos associados.");
        // }

        categoriaRepository.deleteById(id);
    }
}
