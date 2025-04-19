package com.sistema.gestao.socios.repository;

import com.sistema.gestao.socios.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {
    Optional<Socio> findByEmail(String email); // Example custom query
    Optional<Socio> findByDocumento(String documento); // Example custom query
}
