package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.MembrePharmacopee;
import com.pharmacopee.ladafura.enums.StatutMembre;

/**
 * Toutes les recherches d'un membre par son id sont bornées par la pharmacopée
 * (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface MembrePharmacopeeRepository extends JpaRepository<MembrePharmacopee, Long> {

    Optional<MembrePharmacopee> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Optional<MembrePharmacopee> findByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    Optional<MembrePharmacopee> findByUtilisateurIdAndPharmacopeeIdAndStatut(Long utilisateurId, Long pharmacopeeId, StatutMembre statut);

    boolean existsByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    /** Une invitation n'est accessible qu'à l'utilisateur invité. */
    @EntityGraph(attributePaths = "pharmacopee")
    Optional<MembrePharmacopee> findByIdAndUtilisateurIdAndStatut(Long id, Long utilisateurId, StatutMembre statut);

    Optional<MembrePharmacopee> findFirstByPharmacopeeIdAndEstProprietaireTrue(Long pharmacopeeId);

    /** Espaces de travail d'un utilisateur (sélection de la pharmacopée active). */
    @EntityGraph(attributePaths = "pharmacopee")
    List<MembrePharmacopee> findByUtilisateurIdAndStatut(Long utilisateurId, StatutMembre statut);

    @EntityGraph(attributePaths = "utilisateur")
    Page<MembrePharmacopee> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    @EntityGraph(attributePaths = "utilisateur")
    Page<MembrePharmacopee> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutMembre statut, Pageable pageable);

    /** Garde-fou : une pharmacopée doit toujours conserver au moins un propriétaire actif. */
    long countByPharmacopeeIdAndEstProprietaireTrueAndStatut(Long pharmacopeeId, StatutMembre statut);

    /** Consommation du quota MAX_UTILISATEURS_ACTIFS. */
    long countByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutMembre statut);

    /** Membres à réactiver quand le quota le permet. */
    List<MembrePharmacopee> findByPharmacopeeIdAndStatutAndMotifSuspensionOrderByIdAsc(Long pharmacopeeId, StatutMembre statut,
                                                                                       com.pharmacopee.ladafura.enums.MotifSuspensionMembre motif);

    /** Membres à suspendre en cas de dépassement : les plus récents d'abord, jamais le propriétaire. */
    List<MembrePharmacopee> findByPharmacopeeIdAndStatutAndEstProprietaireFalseOrderByDateAdhesionDescIdDesc(Long pharmacopeeId, StatutMembre statut);
}
