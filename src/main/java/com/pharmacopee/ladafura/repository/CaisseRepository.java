package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Caisse;
import com.pharmacopee.ladafura.enums.TypeCaisse;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface CaisseRepository extends JpaRepository<Caisse, Long> {

    Optional<Caisse> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    List<Caisse> findByPharmacopeeIdAndActifTrue(Long pharmacopeeId);

    List<Caisse> findByPharmacopeeIdOrderByNomAsc(Long pharmacopeeId);

    Optional<Caisse> findFirstByPharmacopeeIdAndTypeAndActifTrue(Long pharmacopeeId, TypeCaisse type);

    /** Verrou : sérialise les opérations d'une même caisse pour le contrôle du solde. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Caisse c where c.id = :id and c.pharmacopee.id = :pharmacopeeId")
    Optional<Caisse> findByIdAndPharmacopeeIdForUpdate(@Param("id") Long id, @Param("pharmacopeeId") Long pharmacopeeId);
}
