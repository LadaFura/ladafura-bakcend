package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Favori;

@Repository
public interface FavoriRepository extends JpaRepository<Favori, Long> {

    List<Favori> findByUtilisateurId(Long utilisateurId);

    Page<Favori> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    Optional<Favori> findByUtilisateurIdAndPlanteId(Long utilisateurId, Long planteId);

    Optional<Favori> findByUtilisateurIdAndProduitId(Long utilisateurId, Long produitId);

    Optional<Favori> findByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    boolean existsByUtilisateurIdAndPlanteId(Long utilisateurId, Long planteId);

    boolean existsByUtilisateurIdAndProduitId(Long utilisateurId, Long produitId);

    boolean existsByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    void deleteByUtilisateurIdAndPlanteId(Long utilisateurId, Long planteId);

    void deleteByUtilisateurIdAndProduitId(Long utilisateurId, Long produitId);

    void deleteByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    long countByUtilisateurId(Long utilisateurId);

    Page<Favori> findByUtilisateurIdAndPlanteIsNotNull(Long utilisateurId, Pageable pageable);

    Page<Favori> findByUtilisateurIdAndProduitIsNotNull(Long utilisateurId, Pageable pageable);

    Page<Favori> findByUtilisateurIdAndPharmacopeeIsNotNull(Long utilisateurId, Pageable pageable);

    long countByUtilisateurIdAndPlanteIsNotNull(Long utilisateurId);

    long countByUtilisateurIdAndProduitIsNotNull(Long utilisateurId);

    long countByUtilisateurIdAndPharmacopeeIsNotNull(Long utilisateurId);

    Optional<Favori> findByIdAndUtilisateurId(Long id, Long utilisateurId);
}
