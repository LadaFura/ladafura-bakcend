package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LigneCommande;

@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    List<LigneCommande> findByCommandeId(Long commandeId);

    List<LigneCommande> findByProduitId(Long produitId);

    long countByProduitId(Long produitId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(lc) > 0 FROM LigneCommande lc WHERE lc.commande.utilisateur.id = :utilisateurId AND lc.produit.id = :produitId AND lc.commande.statut IN (com.pharmacopee.ladafura.enums.StatutCommande.LIVREE, com.pharmacopee.ladafura.enums.StatutCommande.RETIREE)")
    boolean hasUserPurchasedAndReceivedProduct(@org.springframework.data.repository.query.Param("utilisateurId") Long utilisateurId, @org.springframework.data.repository.query.Param("produitId") Long produitId);
}
