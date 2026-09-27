package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.EtudeScientifique;

@Repository
public interface EtudeScientifiqueRepository extends JpaRepository<EtudeScientifique, Long> {

    List<EtudeScientifique> findByPlanteId(Long planteId);

    Page<EtudeScientifique> findByPlanteId(Long planteId, Pageable pageable);

    List<EtudeScientifique> findByTitreContainingIgnoreCase(String keyword);
}
