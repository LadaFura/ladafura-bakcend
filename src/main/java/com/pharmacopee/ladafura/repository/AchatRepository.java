package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Achat;
import com.pharmacopee.ladafura.enums.StatutAchat;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface AchatRepository extends JpaRepository<Achat, Long> {

    Optional<Achat> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    /** Dernier achat de l'année, pour la numérotation propre à chaque pharmacopée. */
    Optional<Achat> findFirstByPharmacopeeIdAndNumeroAchatStartingWithOrderByNumeroAchatDesc(Long pharmacopeeId, String prefixe);

    /** Liste filtrée d'une pharmacopée, filtres facultatifs. */
    @Query("""
           select a from Achat a left join fetch a.fournisseur
           where a.pharmacopee.id = :pharmacopeeId
             and (:statut is null or a.statut = :statut)
             and (:fournisseurId is null or a.fournisseur.id = :fournisseurId)
           """)
    Page<Achat> rechercher(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAchat statut,
                           @Param("fournisseurId") Long fournisseurId, Pageable pageable);

    Page<Achat> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Achat> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutAchat statut, Pageable pageable);

    /** Historique des achats auprès d'un fournisseur. */
    Page<Achat> findByPharmacopeeIdAndFournisseurId(Long pharmacopeeId, Long fournisseurId, Pageable pageable);

    /** Un fournisseur lié à des achats ne se supprime pas. */
    boolean existsByFournisseurId(Long fournisseurId);
}
