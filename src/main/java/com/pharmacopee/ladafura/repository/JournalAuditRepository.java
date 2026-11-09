package com.pharmacopee.ladafura.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.JournalAudit;
import com.pharmacopee.ladafura.enums.OrigineAction;
import com.pharmacopee.ladafura.enums.TypeActionAudit;

/**
 * Journal en ajout seul : aucune méthode de modification ne doit être utilisée.
 */
@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    Page<JournalAudit> findByPharmacopeeIdOrderByDateActionDesc(Long pharmacopeeId, Pageable pageable);

    Page<JournalAudit> findAllByOrderByDateActionDesc(Pageable pageable);

    /** Recherche filtrée, du plus récent au plus ancien ; chaque filtre est facultatif. */
    @EntityGraph(attributePaths = {"utilisateur", "pharmacopee"})
    @Query("""
           select j from JournalAudit j
           where (:pharmacopeeId is null or j.pharmacopee.id = :pharmacopeeId)
             and (:action is null or j.action = :action)
             and (:origine is null or j.origine = :origine)
             and (:typeEntite is null or j.typeEntite = :typeEntite)
             and (:debut is null or j.dateAction >= :debut)
             and (:fin is null or j.dateAction <= :fin)
           order by j.dateAction desc, j.id desc
           """)
    Page<JournalAudit> rechercher(@Param("pharmacopeeId") Long pharmacopeeId, @Param("action") TypeActionAudit action,
                                  @Param("origine") OrigineAction origine, @Param("typeEntite") String typeEntite,
                                  @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin, Pageable pageable);
}
