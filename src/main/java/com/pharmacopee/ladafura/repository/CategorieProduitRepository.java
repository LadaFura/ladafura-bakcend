package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.CategorieProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, Long> {

    Optional<CategorieProduit> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<CategorieProduit> findByStatutTrue();
}
