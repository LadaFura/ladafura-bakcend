package com.pharmacopee.ladafura.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Abonnement;
import com.pharmacopee.ladafura.enums.StatutAbonnement;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<StatutAbonnement> STATUTS_EN_VIGUEUR = List.of(StatutAbonnement.ACTIF, StatutAbonnement.EN_GRACE);

    Optional<Abonnement> findFirstByPharmacopeeIdAndStatutIn(Long pharmacopeeId, List<StatutAbonnement> statuts);

    /** Abonnement qui s'applique actuellement à la pharmacopée (au plus un, garanti par V4). */
    default Optional<Abonnement> findEnVigueur(Long pharmacopeeId) {
        return findFirstByPharmacopeeIdAndStatutIn(pharmacopeeId, STATUTS_EN_VIGUEUR);
    }

    Optional<Abonnement> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<Abonnement> findByPharmacopeeIdOrderByDateCreationDesc(Long pharmacopeeId, Pageable pageable);

    /** Abonnements échus, pour la tâche planifiée. */
    List<Abonnement> findByStatutAndDateFinBefore(StatutAbonnement statut, LocalDateTime date);

    /** Périodes de grâce terminées, pour la tâche planifiée. */
    List<Abonnement> findByStatutAndDateFinGraceBefore(StatutAbonnement statut, LocalDateTime date);
}
