package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.enums.StatutAchat;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface AchatRepository extends JpaRepository<Achat, Long> {

    Optional<Achat> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<Achat> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Achat> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutAchat statut, Pageable pageable);

    /** Historique des achats auprès d'un fournisseur. */
    Page<Achat> findByPharmacopeeIdAndFournisseurId(Long pharmacopeeId, Long fournisseurId, Pageable pageable);

    /** Un fournisseur lié à des achats ne se supprime pas. */
    boolean existsByFournisseurId(Long fournisseurId);
}
