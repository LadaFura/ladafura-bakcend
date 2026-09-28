package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.enums.StatutValidation;

@Repository
public interface VertuDeLaPlanteRepository extends JpaRepository<VertuDeLaPlante, Long> {

    List<VertuDeLaPlante> findByPlanteId(Long planteId);

    List<VertuDeLaPlante> findByCollecteId(Long collecteId);

    java.util.Optional<VertuDeLaPlante> findByCollecteIdAndPlanteId(Long collecteId, Long planteId);

    java.util.Optional<VertuDeLaPlante> findByIdAndCollecteId(Long id, Long collecteId);

    boolean existsByCollecteIdAndPlanteId(Long collecteId, Long planteId);

    List<VertuDeLaPlante> findByStatut(StatutValidation statut);

    Page<VertuDeLaPlante> findByStatut(StatutValidation statut, Pageable pageable);

    long countByStatut(StatutValidation statut);
}
