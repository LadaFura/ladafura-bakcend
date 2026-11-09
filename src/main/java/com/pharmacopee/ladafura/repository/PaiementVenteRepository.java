package com.pharmacopee.ladafura.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.PaiementVente;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface PaiementVenteRepository extends JpaRepository<PaiementVente, Long> {

    Optional<PaiementVente> findByIdAndVentePharmacopeeId(Long id, Long pharmacopeeId);

    List<PaiementVente> findByVenteId(Long venteId);

    /** Montant payé d'une vente : somme des paiements valides. */
    @Query("""
           select coalesce(sum(p.montant), 0BD) from PaiementVente p
           where p.vente.id = :venteId and p.statut = com.pharmacopee.ladafura.enums.StatutEcriture.VALIDE
           """)
    BigDecimal sumMontantValide(@Param("venteId") Long venteId);
}
