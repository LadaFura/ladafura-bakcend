package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.FournisseurProduit;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface FournisseurProduitRepository extends JpaRepository<FournisseurProduit, Long> {

    Optional<FournisseurProduit> findByIdAndFournisseurPharmacopeeId(Long id, Long pharmacopeeId);

    @EntityGraph(attributePaths = "produit")
    Page<FournisseurProduit> findByFournisseurId(Long fournisseurId, Pageable pageable);

    boolean existsByFournisseurIdAndProduitId(Long fournisseurId, Long produitId);
}
