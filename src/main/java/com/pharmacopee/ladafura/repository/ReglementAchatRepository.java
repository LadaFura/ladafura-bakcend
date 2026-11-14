package com.pharmacopee.ladafura.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.ReglementAchat;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface ReglementAchatRepository extends JpaRepository<ReglementAchat, Long> {

    Optional<ReglementAchat> findByIdAndAchatPharmacopeeId(Long id, Long pharmacopeeId);

    List<ReglementAchat> findByAchatId(Long achatId);

    /** Montants payés d'une page d'achats, en une requête : lignes [achatId, somme]. */
    @Query("""
           select r.achat.id, sum(r.montant) from ReglementAchat r
           where r.achat.id in :achatIds and r.statut = com.pharmacopee.ladafura.enums.StatutEcriture.VALIDE
           group by r.achat.id
           """)
    List<Object[]> sommesValidesParAchat(@Param("achatIds") java.util.Collection<Long> achatIds);

    /** Montant payé d'un achat : somme des règlements valides. */
    @Query("""
           select coalesce(sum(r.montant), 0BD) from ReglementAchat r
           where r.achat.id = :achatId and r.statut = com.pharmacopee.ladafura.enums.StatutEcriture.VALIDE
           """)
    BigDecimal sumMontantValide(@Param("achatId") Long achatId);
}
