package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Commande;
import com.pharmacopee.ladafura.enums.StatutCommande;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    Optional<Commande> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Commande> findByUtilisateurId(Long utilisateurId);

    Page<Commande> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    long countByUtilisateurId(Long utilisateurId);

    List<Commande> findByPharmacopeeId(Long pharmacopeeId);

    Page<Commande> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    List<Commande> findByStatut(StatutCommande statut);

    Page<Commande> findByStatut(StatutCommande statut, Pageable pageable);

    long countByStatut(StatutCommande statut);

    long countByPharmacopeeId(Long pharmacopeeId);

    Page<Commande> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutCommande statut, Pageable pageable);

    Optional<Commande> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    long countByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutCommande statut);

    @Query("SELECT COALESCE(SUM(c.montantTotal), 0.0) FROM Commande c WHERE c.pharmacopee.id = :pharmacopeeId AND c.statut IN (:statuts)")
    Double sumMontantTotalByPharmacopeeIdAndStatuts(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statuts") List<StatutCommande> statuts);

    @Query("SELECT COALESCE(SUM(c.montantTotal), 0.0) FROM Commande c WHERE c.statut = :statut")
    Double sumMontantTotalByStatut(@Param("statut") StatutCommande statut);

    @Query("SELECT COALESCE(SUM(c.montantTotal), 0.0) FROM Commande c")
    Double sumAllMontantTotal();
}
