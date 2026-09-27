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
}
