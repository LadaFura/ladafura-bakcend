package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LigneVente;

@Repository
public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    @EntityGraph(attributePaths = "disponibiliteProduit")
    List<LigneVente> findByVenteId(Long venteId);
}
