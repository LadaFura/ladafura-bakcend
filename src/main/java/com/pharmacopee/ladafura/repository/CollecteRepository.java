package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.enums.StatutCollecte;

@Repository
public interface CollecteRepository extends JpaRepository<Collecte, Long> {

    List<Collecte> findByStatut(StatutCollecte statut);

    Page<Collecte> findByStatut(StatutCollecte statut, Pageable pageable);

    List<Collecte> findByAgentCollecteId(Long agentId);

    Page<Collecte> findByAgentCollecteId(Long agentId, Pageable pageable);

    Page<Collecte> findByAgentCollecteIdAndStatut(Long agentId, StatutCollecte statut, Pageable pageable);

    List<Collecte> findByAgentCollecteIdAndStatut(Long agentId, StatutCollecte statut);

    List<Collecte> findBySourceId(Long sourceId);

    long countByStatut(StatutCollecte statut);

    long countByAgentCollecteId(Long agentId);

    long countByAgentCollecteIdAndStatut(Long agentId, StatutCollecte statut);

    @Query(value = "SELECT DISTINCT c FROM Collecte c " +
           "LEFT JOIN c.localisation l " +
           "LEFT JOIN c.source s " +
           "LEFT JOIN c.vertus v " +
           "LEFT JOIN v.plante p " +
           "WHERE c.agentCollecte.id = :agentId " +
           "AND (:statut IS NULL OR c.statut = :statut) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.region) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.cercle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.commune) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.localite) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.prenom) LIKE LOWER(CONCAT('%', :query, '%')))",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Collecte c " +
           "LEFT JOIN c.localisation l " +
           "LEFT JOIN c.source s " +
           "LEFT JOIN c.vertus v " +
           "LEFT JOIN v.plante p " +
           "WHERE c.agentCollecte.id = :agentId " +
           "AND (:statut IS NULL OR c.statut = :statut) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.region) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.cercle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.commune) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.localite) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nomScientifique) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.prenom) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Collecte> searchSuiviCollectes(@Param("agentId") Long agentId,
                                       @Param("statut") StatutCollecte statut,
                                       @Param("query") String query,
                                       Pageable pageable);
}
