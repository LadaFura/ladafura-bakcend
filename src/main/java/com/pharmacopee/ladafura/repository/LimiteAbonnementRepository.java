package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LimiteAbonnement;

@Repository
public interface LimiteAbonnementRepository extends JpaRepository<LimiteAbonnement, Long> {

    Optional<LimiteAbonnement> findByAbonnementIdAndCodeLimite(Long abonnementId, String codeLimite);

    List<LimiteAbonnement> findByAbonnementId(Long abonnementId);
}
