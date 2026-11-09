package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.MouvementStock;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    Page<MouvementStock> findByPharmacopeeIdOrderByDateMouvementDesc(Long pharmacopeeId, Pageable pageable);

    Page<MouvementStock> findByPharmacopeeIdAndDisponibiliteProduitIdOrderByDateMouvementDesc(Long pharmacopeeId,
                                                                                              Long disponibiliteId,
                                                                                              Pageable pageable);

    List<MouvementStock> findByLigneAchatId(Long ligneAchatId);

    List<MouvementStock> findByLigneVenteId(Long ligneVenteId);

    boolean existsByDisponibiliteProduitId(Long disponibiliteProduitId);

    List<MouvementStock> findByLigneCommandeId(Long ligneCommandeId);
}
