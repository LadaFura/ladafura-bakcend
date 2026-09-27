package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

@Repository
public interface PharmacopeeRepository extends JpaRepository<Pharmacopee, Long> {

    List<Pharmacopee> findByStatut(StatutPharmacopee statut);

    Page<Pharmacopee> findByStatut(StatutPharmacopee statut, Pageable pageable);

    Optional<Pharmacopee> findByUtilisateurId(Long utilisateurId);

    long countByStatut(StatutPharmacopee statut);

    List<Pharmacopee> findByNomContainingIgnoreCase(String keyword);
}
