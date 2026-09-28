package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.NomPlante;

@Repository
public interface NomPlanteRepository extends JpaRepository<NomPlante, Long> {

    List<NomPlante> findByPlanteId(Long planteId);

    List<NomPlante> findByNomContainingIgnoreCase(String nom);

    List<NomPlante> findByLangueIgnoreCase(String langue);

    List<NomPlante> findByNomContainingIgnoreCaseAndLangueIgnoreCase(String nom, String langue);

    boolean existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(Long planteId, String nom, String langue);

    java.util.Optional<NomPlante> findByIdAndPlanteId(Long id, Long planteId);
}
