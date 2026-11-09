package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.RolePharmacopee;

@Repository
public interface RolePharmacopeeRepository extends JpaRepository<RolePharmacopee, Long> {

    /** Rôle système partagé (ex. GERANT, VENDEUR_CAISSIER). */
    Optional<RolePharmacopee> findByCodeAndPharmacopeeIsNull(String code);

    /** Rôle personnalisé appartenant à une pharmacopée donnée. */
    Optional<RolePharmacopee> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    boolean existsByPharmacopeeIdAndCode(Long pharmacopeeId, String code);

    /** Rôles utilisables dans une pharmacopée : rôles système + rôles personnalisés de ce tenant. */
    @Query("""
           select r from RolePharmacopee r
           where r.actif = true and (r.pharmacopee is null or r.pharmacopee.id = :pharmacopeeId)
           order by r.systeme desc, r.libelle asc
           """)
    List<RolePharmacopee> findDisponiblesPourPharmacopee(@Param("pharmacopeeId") Long pharmacopeeId);

    /** Recherche d'un rôle par id limitée aux rôles visibles par la pharmacopée. */
    @Query("""
           select r from RolePharmacopee r
           where r.id = :id and (r.pharmacopee is null or r.pharmacopee.id = :pharmacopeeId)
           """)
    Optional<RolePharmacopee> findDisponibleByIdPourPharmacopee(@Param("id") Long id, @Param("pharmacopeeId") Long pharmacopeeId);
}
