package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.CategorieProduit;

@Repository
public interface CategorieProduitRepository extends JpaRepository<CategorieProduit, Long> {

    Optional<CategorieProduit> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<CategorieProduit> findByStatutTrue();
}
