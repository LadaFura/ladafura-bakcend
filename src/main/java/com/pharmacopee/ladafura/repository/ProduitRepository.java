package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Produit;
import com.pharmacopee.ladafura.enums.StatutProduit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    List<Produit> findByStatut(StatutProduit statut);

    Optional<Produit> findByIdAndStatut(Long id, StatutProduit statut);

    Page<Produit> findByStatut(StatutProduit statut, Pageable pageable);

    List<Produit> findByCategorieId(Long categorieId);

    Page<Produit> findByCategorieId(Long categorieId, Pageable pageable);

    List<Produit> findByNomContainingIgnoreCase(String keyword);

    Page<Produit> findByNomContainingIgnoreCase(String keyword, Pageable pageable);

    long countByStatut(StatutProduit statut);

    long countByCategorieId(Long categorieId);

    @Query("SELECT p FROM Produit p WHERE p.statut = :statut " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categorieId IS NULL OR p.categorie.id = :categorieId) " +
           "AND (:prixMax IS NULL OR p.prix <= :prixMax)")
    Page<Produit> searchProduits(@Param("statut") StatutProduit statut,
                                 @Param("keyword") String keyword,
                                 @Param("categorieId") Long categorieId,
                                 @Param("prixMax") Double prixMax,
                                 Pageable pageable);

    @Query("SELECT p FROM Produit p WHERE p.statut = :statut " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Produit> searchTopByStatutAndKeyword(@Param("statut") StatutProduit statut,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);
}
