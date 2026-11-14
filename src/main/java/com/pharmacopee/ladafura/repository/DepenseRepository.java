package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Depense;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface DepenseRepository extends JpaRepository<Depense, Long> {

    Optional<Depense> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    /** Liste d'une pharmacopée, période facultative. */
    @Query("""
           select d from Depense d
           where d.pharmacopee.id = :pharmacopeeId
             and (:debut is null or d.dateDepense >= :debut)
             and (:fin is null or d.dateDepense <= :fin)
           order by d.dateDepense desc, d.id desc
           """)
    Page<Depense> rechercher(@Param("pharmacopeeId") Long pharmacopeeId, @Param("debut") java.time.LocalDateTime debut,
                             @Param("fin") java.time.LocalDateTime fin, Pageable pageable);

    /** Dépenses valides par catégorie sur une période : lignes [catégorie, somme]. */
    @Query("""
           select d.categorie, sum(d.montant) from Depense d
           where d.pharmacopee.id = :pharmacopeeId and d.statut = com.pharmacopee.ladafura.enums.StatutEcriture.VALIDE
             and d.dateDepense between :debut and :fin
           group by d.categorie
           """)
    java.util.List<Object[]> totauxParCategorie(@Param("pharmacopeeId") Long pharmacopeeId, @Param("debut") java.time.LocalDateTime debut, @Param("fin") java.time.LocalDateTime fin);

    Page<Depense> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
