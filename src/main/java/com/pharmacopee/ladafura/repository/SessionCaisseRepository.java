package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.SessionCaisse;
import com.pharmacopee.ladafura.enums.StatutSessionCaisse;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {

    Optional<SessionCaisse> findByIdAndCaissePharmacopeeId(Long id, Long pharmacopeeId);

    Optional<SessionCaisse> findFirstByCaisseIdAndStatut(Long caisseId, StatutSessionCaisse statut);

    Optional<SessionCaisse> findFirstByCaisseIdOrderByDateOuvertureDesc(Long caisseId);

    Page<SessionCaisse> findByCaisseIdOrderByDateOuvertureDesc(Long caisseId, Pageable pageable);
}
