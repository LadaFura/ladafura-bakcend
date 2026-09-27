package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.enums.StatutPlante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanteRepository extends JpaRepository<Plante, Long> {

    Optional<Plante> findByNomScientifiqueIgnoreCase(String nomScientifique);

    boolean existsByNomScientifiqueIgnoreCase(String nomScientifique);

    List<Plante> findByStatut(StatutPlante statut);

    Page<Plante> findByStatut(StatutPlante statut, Pageable pageable);

    long countByStatut(StatutPlante statut);

    @Query("SELECT DISTINCT p FROM Plante p LEFT JOIN p.nomsPlante n " +
            "WHERE LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Plante> searchByNomScientifiqueOrNomVernaculaire(@Param("keyword") String keyword);
}
