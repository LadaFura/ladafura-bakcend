package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.DisponibiliteProduit;

@Repository
public interface DisponibiliteProduitRepository extends JpaRepository<DisponibiliteProduit, Long> {

    List<DisponibiliteProduit> findByPharmacopeeId(Long pharmacopeeId);

    Page<DisponibiliteProduit> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<DisponibiliteProduit> findByPharmacopeeIdAndDisponible(Long pharmacopeeId, Boolean disponible, Pageable pageable);

    List<DisponibiliteProduit> findByProduitId(Long produitId);

    List<DisponibiliteProduit> findByProduitIdAndDisponibleTrue(Long produitId);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM DisponibiliteProduit d JOIN FETCH d.pharmacopee ph LEFT JOIN FETCH ph.localisation WHERE d.produit.id = :produitId AND ph.statut = com.pharmacopee.ladafura.enums.StatutPharmacopee.VALIDEE")
    List<DisponibiliteProduit> findOffresValideesByProduitId(@org.springframework.data.repository.query.Param("produitId") Long produitId);

    Optional<DisponibiliteProduit> findByPharmacopeeIdAndProduitId(Long pharmacopeeId, Long produitId);

    boolean existsByPharmacopeeIdAndProduitId(Long pharmacopeeId, Long produitId);

    long countByPharmacopeeId(Long pharmacopeeId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT d FROM DisponibiliteProduit d JOIN FETCH d.produit p LEFT JOIN FETCH p.categorie WHERE d.pharmacopee.id = :pharmacopeeId AND p.statut = com.pharmacopee.ladafura.enums.StatutProduit.VALIDE AND (:disponibleOnly IS NULL OR :disponibleOnly = false OR d.disponible = true)",
           countQuery = "SELECT COUNT(d) FROM DisponibiliteProduit d WHERE d.pharmacopee.id = :pharmacopeeId AND d.produit.statut = com.pharmacopee.ladafura.enums.StatutProduit.VALIDE AND (:disponibleOnly IS NULL OR :disponibleOnly = false OR d.disponible = true)")
    Page<DisponibiliteProduit> findProduitsByPharmacopeeId(
            @org.springframework.data.repository.query.Param("pharmacopeeId") Long pharmacopeeId,
            @org.springframework.data.repository.query.Param("disponibleOnly") Boolean disponibleOnly,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(d) FROM DisponibiliteProduit d WHERE d.pharmacopee.id = :pharmacopeeId AND d.produit.statut = com.pharmacopee.ladafura.enums.StatutProduit.VALIDE")
    long countProduitsValidesByPharmacopeeId(@org.springframework.data.repository.query.Param("pharmacopeeId") Long pharmacopeeId);
}
