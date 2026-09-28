package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LignePanier;

@Repository
public interface LignePanierRepository extends JpaRepository<LignePanier, Long> {

    List<LignePanier> findByPanierId(Long panierId);

    Optional<LignePanier> findByPanierIdAndProduitId(Long panierId, Long produitId);

    Optional<LignePanier> findByIdAndPanierId(Long id, Long panierId);

    void deleteByPanierId(Long panierId);
}
