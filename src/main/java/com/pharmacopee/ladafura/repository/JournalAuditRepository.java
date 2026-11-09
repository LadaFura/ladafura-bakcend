package com.pharmacopee.ladafura.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.JournalAudit;

/**
 * Journal en ajout seul : aucune méthode de modification ne doit être utilisée.
 */
@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {

    Page<JournalAudit> findByPharmacopeeIdOrderByDateActionDesc(Long pharmacopeeId, Pageable pageable);

    Page<JournalAudit> findAllByOrderByDateActionDesc(Pageable pageable);
}
