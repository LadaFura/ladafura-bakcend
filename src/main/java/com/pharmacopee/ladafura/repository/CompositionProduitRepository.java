package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.CompositionProduit;

@Repository
public interface CompositionProduitRepository extends JpaRepository<CompositionProduit, Long> {

    List<CompositionProduit> findByProduitId(Long produitId);

    List<CompositionProduit> findByPlanteId(Long planteId);

    void deleteByProduitId(Long produitId);
}
