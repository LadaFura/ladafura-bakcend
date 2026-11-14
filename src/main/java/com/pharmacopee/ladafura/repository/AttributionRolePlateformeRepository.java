package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.AttributionRolePlateforme;

@Repository
public interface AttributionRolePlateformeRepository extends JpaRepository<AttributionRolePlateforme, Long> {

    List<AttributionRolePlateforme> findByUtilisateurId(Long utilisateurId);

    Optional<AttributionRolePlateforme> findByUtilisateurIdAndRolePlateformeId(Long utilisateurId, Long rolePlateformeId);

    boolean existsByUtilisateurIdAndRolePlateformeCode(Long utilisateurId, String code);

    long countByRolePlateformeCode(String code);

    /** Codes des rôles de plateforme actifs d'un utilisateur. */
    @Query("""
           select a.rolePlateforme.code
           from AttributionRolePlateforme a
           where a.utilisateur.id = :utilisateurId and a.rolePlateforme.actif = true
           """)
    Set<String> findCodesRolesActifsByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
