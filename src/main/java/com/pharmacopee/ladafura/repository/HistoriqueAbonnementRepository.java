package com.pharmacopee.ladafura.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.HistoriqueAbonnement;

@Repository
public interface HistoriqueAbonnementRepository extends JpaRepository<HistoriqueAbonnement, Long> {

    Page<HistoriqueAbonnement> findByAbonnementIdOrderByDateEvenementDesc(Long abonnementId, Pageable pageable);
}
