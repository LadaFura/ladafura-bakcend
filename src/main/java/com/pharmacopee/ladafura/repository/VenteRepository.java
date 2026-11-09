package com.pharmacopee.ladafura.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.enums.StatutVente;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    Optional<Vente> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<Vente> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Vente> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutVente statut, Pageable pageable);

    /** Consommation du quota MAX_VENTES_MENSUELLES. */
    long countByPharmacopeeIdAndStatutAndDateVenteBetween(Long pharmacopeeId, StatutVente statut,
                                                          LocalDateTime debut, LocalDateTime fin);
}
