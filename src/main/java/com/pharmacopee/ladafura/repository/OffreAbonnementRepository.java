package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.OffreAbonnement;

@Repository
public interface OffreAbonnementRepository extends JpaRepository<OffreAbonnement, Long> {

    Optional<OffreAbonnement> findByCode(String code);

    List<OffreAbonnement> findByActifTrue();
}
