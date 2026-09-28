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

    Optional<DisponibiliteProduit> findByPharmacopeeIdAndProduitId(Long pharmacopeeId, Long produitId);

    boolean existsByPharmacopeeIdAndProduitId(Long pharmacopeeId, Long produitId);

    long countByPharmacopeeId(Long pharmacopeeId);
}
