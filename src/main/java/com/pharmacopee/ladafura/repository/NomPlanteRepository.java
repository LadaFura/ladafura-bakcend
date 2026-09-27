package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.NomPlante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NomPlanteRepository extends JpaRepository<NomPlante, Long> {

    List<NomPlante> findByPlanteId(Long planteId);

    List<NomPlante> findByNomContainingIgnoreCase(String nom);

    List<NomPlante> findByLangueIgnoreCase(String langue);
}
