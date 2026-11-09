package com.pharmacopee.ladafura.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.OperationCaisse;
import com.pharmacopee.ladafura.enums.NatureOperationCaisse;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface OperationCaisseRepository extends JpaRepository<OperationCaisse, Long> {

    Optional<OperationCaisse> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    Page<OperationCaisse> findByCaisseIdOrderByDateOperationDesc(Long caisseId, Pageable pageable);

    /** Une opération n'est contre-passée qu'une seule fois. */
    boolean existsByOperationLieeIdAndNature(Long operationLieeId, NatureOperationCaisse nature);

    /** Entrées moins sorties de toute la caisse. */
    @Query("""
           select coalesce(sum(case when o.sens = com.pharmacopee.ladafura.enums.SensOperation.ENTREE
                                    then o.montant else -o.montant end), 0BD)
           from OperationCaisse o where o.caisse.id = :caisseId
           """)
    BigDecimal soldeTheorique(@Param("caisseId") Long caisseId);

    /** Entrées moins sorties d'une session, hors fond d'ouverture. */
    @Query("""
           select coalesce(sum(case when o.sens = com.pharmacopee.ladafura.enums.SensOperation.ENTREE
                                    then o.montant else -o.montant end), 0BD)
           from OperationCaisse o where o.session.id = :sessionId
           """)
    BigDecimal soldeSession(@Param("sessionId") Long sessionId);
}
