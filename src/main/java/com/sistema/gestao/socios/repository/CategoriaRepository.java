package com.sistema.gestao.socios.repository;

import com.sistema.gestao.socios.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import Optional

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Find category by name, ignoring case
    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
