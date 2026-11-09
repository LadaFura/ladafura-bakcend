package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Depense;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {

    Optional<Depense> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<Depense> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
