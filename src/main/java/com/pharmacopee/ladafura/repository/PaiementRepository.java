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

    /** Un même numéro de transaction ne couvre qu'un paiement ; un paiement rejeté libère son numéro. */
    @Query("select count(p) > 0 from Paiement p where p.caisse.id = :caisseId and p.reference = :reference "
            + "and p.statut <> :statutExclu and (:paiementId is null or p.id <> :paiementId)")
    boolean referenceDejaDeclaree(@Param("caisseId") Long caisseId, @Param("reference") String reference,
            @Param("statutExclu") StatutPaiement statutExclu, @Param("paiementId") Long paiementId);

    List<Paiement> findByStatut(StatutPaiement statut);

    Page<Paiement> findByStatut(StatutPaiement statut, Pageable pageable);

    long countByStatut(StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(p.montant), 0.0) FROM Paiement p WHERE p.statut = :statut")
    Double sumMontantByStatut(@Param("statut") StatutPaiement statut);

    Page<Paiement> findByCommandePharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Paiement> findByCommandePharmacopeeIdAndStatut(Long pharmacopeeId, StatutPaiement statut, Pageable pageable);

    Page<Paiement> findByCommandePharmacopeeIdAndMethode(Long pharmacopeeId, com.pharmacopee.ladafura.enums.MethodePaiement methode, Pageable pageable);

    Optional<Paiement> findByIdAndCommandePharmacopeeId(Long id, Long pharmacopeeId);

    Optional<Paiement> findByCommandeIdAndCommandePharmacopeeId(Long commandeId, Long pharmacopeeId);

    Optional<Paiement> findByCommandeIdAndCommandeUtilisateurId(Long commandeId, Long utilisateurId);

    Page<Paiement> findByCommandeUtilisateurId(Long utilisateurId, Pageable pageable);

    List<Paiement> findByCommandeUtilisateurId(Long utilisateurId);

    Page<Paiement> findByCommandeUtilisateurIdAndStatut(Long utilisateurId, StatutPaiement statut, Pageable pageable);

    List<Paiement> findByCommandeUtilisateurIdAndStatut(Long utilisateurId, StatutPaiement statut);

    long countByCommandePharmacopeeIdAndStatut(Long pharmacopeeId, StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(p.montant), 0.0) FROM Paiement p WHERE p.commande.pharmacopee.id = :pharmacopeeId AND p.statut = :statut")
    Double sumMontantByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutPaiement statut);

    @Query("SELECT COALESCE(SUM(p.montant), 0.0) FROM Paiement p WHERE p.commande.pharmacopee.id = :pharmacopeeId AND p.methode = :methode AND p.statut = :statut")
    Double sumMontantByPharmacopeeIdAndMethodeAndStatut(
            @Param("pharmacopeeId") Long pharmacopeeId,
            @Param("methode") com.pharmacopee.ladafura.enums.MethodePaiement methode,
            @Param("statut") StatutPaiement statut);
}
