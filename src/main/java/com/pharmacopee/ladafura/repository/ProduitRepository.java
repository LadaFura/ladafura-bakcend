package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.enums.StatutProduit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    List<Produit> findByStatut(StatutProduit statut);

    Page<Produit> findByStatut(StatutProduit statut, Pageable pageable);

    List<Produit> findByCategorieId(Long categorieId);

    Page<Produit> findByCategorieId(Long categorieId, Pageable pageable);

    List<Produit> findByNomContainingIgnoreCase(String keyword);

    Page<Produit> findByNomContainingIgnoreCase(String keyword, Pageable pageable);

    long countByStatut(StatutProduit statut);

    long countByCategorieId(Long categorieId);
}
