package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Client;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Optional<Client> findByPharmacopeeIdAndTelephone(Long pharmacopeeId, String telephone);

    Page<Client> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);
}
