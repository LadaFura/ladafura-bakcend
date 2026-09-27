package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Panier;

@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {

    Optional<Panier> findByUtilisateurId(Long utilisateurId);

    boolean existsByUtilisateurId(Long utilisateurId);

    void deleteByUtilisateurId(Long utilisateurId);
}
