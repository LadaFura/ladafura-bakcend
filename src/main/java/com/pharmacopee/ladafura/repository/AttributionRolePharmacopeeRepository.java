package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.AttributionRolePharmacopee;

@Repository
public interface AttributionRolePharmacopeeRepository extends JpaRepository<AttributionRolePharmacopee, Long> {

    @EntityGraph(attributePaths = "rolePharmacopee")
    List<AttributionRolePharmacopee> findByMembrePharmacopeeId(Long membrePharmacopeeId);

    @EntityGraph(attributePaths = "rolePharmacopee")
    List<AttributionRolePharmacopee> findByMembrePharmacopeeIdIn(java.util.Collection<Long> membreIds);

    /** Recherche bornée par la pharmacopée du membre (isolation multi-tenant). */
    Optional<AttributionRolePharmacopee> findByIdAndMembrePharmacopeePharmacopeeId(Long id, Long pharmacopeeId);

    Optional<AttributionRolePharmacopee> findByMembrePharmacopeeIdAndRolePharmacopeeId(Long membreId, Long roleId);

    boolean existsByMembrePharmacopeeIdAndRolePharmacopeeId(Long membreId, Long roleId);

    /** Empêche la suppression d'un rôle personnalisé encore attribué. */
    long countByRolePharmacopeeId(Long rolePharmacopeeId);
}
