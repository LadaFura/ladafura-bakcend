package com.pharmacopee.ladafura.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.DemandeContrat;
import com.pharmacopee.ladafura.enums.StatutDemandeContrat;

@Repository
public interface DemandeContratRepository extends JpaRepository<DemandeContrat, Long> {

    Page<DemandeContrat> findByStatut(StatutDemandeContrat statut, Pageable pageable);

    Page<DemandeContrat> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
