package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Client;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    /** Recherche facultative sur le nom, le prénom ou le téléphone. */
    @Query("""
           select c from Client c
           where c.pharmacopee.id = :pharmacopeeId
             and (:motif is null
                  or lower(c.nom) like :motif
                  or lower(c.prenom) like :motif
                  or c.telephone like :motif)
           """)
    Page<Client> rechercher(@Param("pharmacopeeId") Long pharmacopeeId, @Param("motif") String motif, Pageable pageable);

    Optional<Client> findByPharmacopeeIdAndTelephone(Long pharmacopeeId, String telephone);

    Page<Client> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
