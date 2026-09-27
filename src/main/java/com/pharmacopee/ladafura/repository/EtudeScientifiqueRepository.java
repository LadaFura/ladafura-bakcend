package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.EtudeScientifique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudeScientifiqueRepository extends JpaRepository<EtudeScientifique, Long> {

    List<EtudeScientifique> findByPlanteId(Long planteId);

    Page<EtudeScientifique> findByPlanteId(Long planteId, Pageable pageable);

    List<EtudeScientifique> findByTitreContainingIgnoreCase(String keyword);
}
