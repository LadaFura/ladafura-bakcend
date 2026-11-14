package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Fournisseur;
import com.pharmacopee.ladafura.enums.StatutFournisseur;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {

    Optional<Fournisseur> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<Fournisseur> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Fournisseur> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutFournisseur statut, Pageable pageable);
}
