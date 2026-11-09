package com.pharmacopee.ladafura.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Lot;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    Optional<Lot> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    /** Lots vendables à la date donnée : les plus proches de la péremption d'abord, sans date en dernier. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select l from Lot l
           where l.disponibiliteProduit.id = :disponibiliteId
             and l.quantiteRestante > 0
             and (l.datePeremption is null or l.datePeremption >= :date)
           order by case when l.datePeremption is null then 1 else 0 end, l.datePeremption asc, l.id asc
           """)
    List<Lot> findLotsVendablesForUpdate(@Param("disponibiliteId") Long disponibiliteId, @Param("date") LocalDate date);

    /** Quantité totale suivie par lot, périmés compris. */
    @Query("select coalesce(sum(l.quantiteRestante), 0) from Lot l where l.disponibiliteProduit.id = :disponibiliteId")
    int sumQuantiteRestante(@Param("disponibiliteId") Long disponibiliteId);
}
