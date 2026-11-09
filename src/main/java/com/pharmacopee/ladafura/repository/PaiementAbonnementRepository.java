package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.PaiementAbonnement;
import com.pharmacopee.ladafura.enums.ModePaiementAbonnement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

@Repository
public interface PaiementAbonnementRepository extends JpaRepository<PaiementAbonnement, Long> {

    Optional<PaiementAbonnement> findByReference(String reference);

    Optional<PaiementAbonnement> findByIdAndAbonnementPharmacopeeId(Long id, Long pharmacopeeId);

    Page<PaiementAbonnement> findByAbonnementPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<PaiementAbonnement> findByStatut(StatutPaiement statut, Pageable pageable);

    boolean existsByAbonnementIdAndStatut(Long abonnementId, StatutPaiement statut);

    /** Une référence de transaction ne sert jamais à deux paiements confirmés. */
    boolean existsByModePaiementAndReferenceTransactionAndStatut(ModePaiementAbonnement modePaiement,
                                                                 String referenceTransaction,
                                                                 StatutPaiement statut);
}
