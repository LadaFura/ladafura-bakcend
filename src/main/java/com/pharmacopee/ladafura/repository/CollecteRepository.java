package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.Collecte;
import com.pharmacopee.ladafura.enums.StatutCollecte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollecteRepository extends JpaRepository<Collecte, Long> {

    List<Collecte> findByStatut(StatutCollecte statut);

    Page<Collecte> findByStatut(StatutCollecte statut, Pageable pageable);

    List<Collecte> findByAgentCollecteId(Long agentId);

    Page<Collecte> findByAgentCollecteId(Long agentId, Pageable pageable);

    List<Collecte> findBySourceId(Long sourceId);

    long countByStatut(StatutCollecte statut);
}
