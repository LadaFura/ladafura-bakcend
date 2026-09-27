package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.enums.StatutAvis;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByProduitId(Long produitId);

    Page<Avis> findByProduitId(Long produitId, Pageable pageable);

    List<Avis> findByUtilisateurId(Long utilisateurId);

    List<Avis> findByStatut(StatutAvis statut);

    Page<Avis> findByStatut(StatutAvis statut, Pageable pageable);

    long countByStatut(StatutAvis statut);

    long countByProduitId(Long produitId);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.produit.id = :produitId AND a.statut = :statut")
    Double findAverageNoteByProduitIdAndStatut(@Param("produitId") Long produitId, @Param("statut") StatutAvis statut);

    Page<Avis> findByProduitIdAndStatut(Long produitId, StatutAvis statut, Pageable pageable);

    long countByProduitIdAndStatut(Long produitId, StatutAvis statut);

    @Query("SELECT a FROM Avis a WHERE a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut")
    Page<Avis> findByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut, Pageable pageable);

    @Query("SELECT a FROM Avis a WHERE a.id = :id AND a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut")
    java.util.Optional<Avis> findByIdAndPharmacopeeIdAndStatut(@Param("id") Long id, @Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut")
    Double findAverageNoteByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut")
    long countByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut AND a.note = :note")
    long countByPharmacopeeIdAndStatutAndNote(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut, @Param("note") Integer note);

    @Query("SELECT COUNT(DISTINCT a.produit.id) FROM Avis a WHERE a.produit.id IN (SELECT dp.produit.id FROM DisponibiliteProduit dp WHERE dp.pharmacopee.id = :pharmacopeeId) AND a.statut = :statut")
    int countDistinctProduitsWithAvisByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut);
}
