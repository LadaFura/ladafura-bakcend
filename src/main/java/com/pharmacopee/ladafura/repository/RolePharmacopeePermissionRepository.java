package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.RolePharmacopeePermission;

@Repository
public interface RolePharmacopeePermissionRepository extends JpaRepository<RolePharmacopeePermission, Long> {

    List<RolePharmacopeePermission> findByRolePharmacopeeId(Long rolePharmacopeeId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "permission")
    List<RolePharmacopeePermission> findByRolePharmacopeeIdIn(java.util.Collection<Long> roleIds);

    boolean existsByRolePharmacopeeIdAndPermissionId(Long rolePharmacopeeId, Long permissionId);

    void deleteByRolePharmacopeeIdAndPermissionId(Long rolePharmacopeeId, Long permissionId);

    /**
     * Codes de permissions effectifs d'un utilisateur dans une pharmacopée :
     * membre ACTIF, rôles actifs, rôles système ou propres à cette pharmacopée.
     */
    @Query("""
           select distinct rpp.permission.code
           from RolePharmacopeePermission rpp, AttributionRolePharmacopee a
           where a.rolePharmacopee = rpp.rolePharmacopee
             and a.membrePharmacopee.utilisateur.id = :utilisateurId
             and a.membrePharmacopee.pharmacopee.id = :pharmacopeeId
             and a.membrePharmacopee.statut = com.pharmacopee.ladafura.enums.StatutMembre.ACTIF
             and rpp.rolePharmacopee.actif = true
             and (rpp.rolePharmacopee.pharmacopee is null or rpp.rolePharmacopee.pharmacopee.id = :pharmacopeeId)
           """)
    Set<String> findCodesPermissionsEffectives(@Param("utilisateurId") Long utilisateurId,
                                               @Param("pharmacopeeId") Long pharmacopeeId);
}
