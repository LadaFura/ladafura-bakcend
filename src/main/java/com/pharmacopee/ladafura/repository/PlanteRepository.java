package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Plante;
import com.pharmacopee.ladafura.enums.StatutPlante;

@Repository
public interface PlanteRepository extends JpaRepository<Plante, Long> {

    Optional<Plante> findByNomScientifiqueIgnoreCase(String nomScientifique);

    Optional<Plante> findByIdAndStatut(Long id, StatutPlante statut);

    boolean existsByNomScientifiqueIgnoreCase(String nomScientifique);

    List<Plante> findByStatut(StatutPlante statut);

    Page<Plante> findByStatut(StatutPlante statut, Pageable pageable);

    long countByStatut(StatutPlante statut);

    @Query("SELECT DISTINCT p FROM Plante p LEFT JOIN p.nomsPlante n " +
            "WHERE LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Plante> searchByNomScientifiqueOrNomVernaculaire(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT p FROM Plante p LEFT JOIN p.nomsPlante n " +
            "WHERE LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Plante> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Plante p LEFT JOIN p.nomsPlante n LEFT JOIN p.maladies m " +
            "WHERE p.statut = :statut AND (LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Plante> searchByStatutAndKeyword(@Param("statut") StatutPlante statut, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Plante p LEFT JOIN p.nomsPlante n LEFT JOIN p.maladies m " +
            "WHERE p.statut = :statut AND (LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(n.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Plante> searchTopByStatutAndKeyword(@Param("statut") StatutPlante statut, @Param("keyword") String keyword, Pageable pageable);
}
