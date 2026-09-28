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
}
