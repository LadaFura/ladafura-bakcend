package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.NomPlante;

@Repository
public interface NomPlanteRepository extends JpaRepository<NomPlante, Long> {

    List<NomPlante> findByPlanteId(Long planteId);

    List<NomPlante> findByNomContainingIgnoreCase(String nom);

    Page<NomPlante> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    List<NomPlante> findByLangueIgnoreCase(String langue);

    List<NomPlante> findByNomContainingIgnoreCaseAndLangueIgnoreCase(String nom, String langue);

    boolean existsByPlanteIdAndNomIgnoreCaseAndLangueIgnoreCase(Long planteId, String nom, String langue);

    java.util.Optional<NomPlante> findByIdAndPlanteId(Long id, Long planteId);

    @Query("SELECT n FROM NomPlante n WHERE " +
           "(:nom IS NULL OR :nom = '' OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :nom, '%'))) AND " +
           "(:langue IS NULL OR :langue = '' OR LOWER(n.langue) = LOWER(:langue))")
    Page<NomPlante> searchNomsVernaculaires(@Param("nom") String nom, @Param("langue") String langue, Pageable pageable);
}
