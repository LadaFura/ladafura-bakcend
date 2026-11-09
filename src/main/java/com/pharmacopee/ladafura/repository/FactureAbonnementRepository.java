package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.FactureAbonnement;

@Repository
public interface FactureAbonnementRepository extends JpaRepository<FactureAbonnement, Long> {

    Optional<FactureAbonnement> findByNumeroFacture(String numeroFacture);

    Optional<FactureAbonnement> findByIdAndAbonnementPharmacopeeId(Long id, Long pharmacopeeId);

    Page<FactureAbonnement> findByAbonnementPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
