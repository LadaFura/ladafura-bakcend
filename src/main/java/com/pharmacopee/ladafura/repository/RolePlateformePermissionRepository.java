package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.RolePlateformePermission;

@Repository
public interface RolePlateformePermissionRepository extends JpaRepository<RolePlateformePermission, Long> {

    List<RolePlateformePermission> findByRolePlateformeId(Long rolePlateformeId);

    boolean existsByRolePlateformeIdAndPermissionId(Long rolePlateformeId, Long permissionId);

    void deleteByRolePlateformeIdAndPermissionId(Long rolePlateformeId, Long permissionId);

    /** Codes de permissions de plateforme effectifs d'un utilisateur (rôles actifs uniquement). */
    @Query("""
           select distinct rpp.permission.code
           from RolePlateformePermission rpp, AttributionRolePlateforme a
           where a.rolePlateforme = rpp.rolePlateforme
             and a.utilisateur.id = :utilisateurId
             and rpp.rolePlateforme.actif = true
           """)
    Set<String> findCodesPermissionsByUtilisateurId(@Param("utilisateurId") Long utilisateurId);
}
