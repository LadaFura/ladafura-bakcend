package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Paiement;
import com.pharmacopee.ladafura.enums.StatutPaiement;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    Optional<Paiement> findByCommandeId(Long commandeId);

    Optional<Paiement> findByReference(String reference);

    List<Paiement> findByStatut(StatutPaiement statut);

    Page<Paiement> findByStatut(StatutPaiement statut, Pageable pageable);

    long countByStatut(StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(p.montant), 0.0) FROM Paiement p WHERE p.statut = :statut")
    Double sumMontantByStatut(@Param("statut") StatutPaiement statut);
}
