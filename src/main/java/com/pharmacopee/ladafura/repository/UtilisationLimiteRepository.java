package com.pharmacopee.ladafura.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.UtilisationLimite;

@Repository
public interface UtilisationLimiteRepository extends JpaRepository<UtilisationLimite, Long> {

    Optional<UtilisationLimite> findByLimiteAbonnementIdAndPeriodeDebut(Long limiteAbonnementId, LocalDate periodeDebut);
}
