package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.VertuDeLaPlante;
import com.pharmacopee.ladafura.enums.StatutValidation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VertuDeLaPlanteRepository extends JpaRepository<VertuDeLaPlante, Long> {

    List<VertuDeLaPlante> findByPlanteId(Long planteId);

    List<VertuDeLaPlante> findByCollecteId(Long collecteId);

    List<VertuDeLaPlante> findByStatut(StatutValidation statut);

    Page<VertuDeLaPlante> findByStatut(StatutValidation statut, Pageable pageable);

    long countByStatut(StatutValidation statut);
}
