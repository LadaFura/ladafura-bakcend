package com.pharmacopee.ladafura.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Vente;
import com.pharmacopee.ladafura.enums.StatutVente;

/**
 * Recherches bornées par la pharmacopée (isolation multi-tenant) : ne pas utiliser {@code findById} dans l'espace pharmacopée.
 */
@Repository
public interface VenteRepository extends JpaRepository<Vente, Long> {

    Optional<Vente> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    /** Dernière vente de l'année, pour la numérotation propre à chaque pharmacopée. */
    Optional<Vente> findFirstByPharmacopeeIdAndNumeroVenteStartingWithOrderByNumeroVenteDesc(Long pharmacopeeId, String prefixe);

    /** Liste filtrée d'une pharmacopée, filtres facultatifs. */
    @Query("""
           select v from Vente v left join fetch v.client
           where v.pharmacopee.id = :pharmacopeeId
             and (:statut is null or v.statut = :statut)
             and (:clientId is null or v.client.id = :clientId)
           """)
    Page<Vente> rechercher(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutVente statut,
                           @Param("clientId") Long clientId, Pageable pageable);

    /** Chiffre d'affaires des ventes au comptoir validées sur une période. */
    @Query("""
           select coalesce(sum(v.montantTotal), 0BD) from Vente v
           where v.pharmacopee.id = :pharmacopeeId and v.statut = com.pharmacopee.ladafura.enums.StatutVente.VALIDEE
             and v.dateVente between :debut and :fin
           """)
    java.math.BigDecimal chiffreAffaires(@Param("pharmacopeeId") Long pharmacopeeId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);

    /** Total des ventes validées par client : lignes [clientId, somme]. */
    @Query("""
           select v.client.id, sum(v.montantTotal) from Vente v
           where v.client.id in :clientIds and v.statut = com.pharmacopee.ladafura.enums.StatutVente.VALIDEE
           group by v.client.id
           """)
    List<Object[]> totauxValidesParClient(@Param("clientIds") java.util.Collection<Long> clientIds);

    Page<Vente> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Vente> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutVente statut, Pageable pageable);

    /** Consommation du quota MAX_VENTES_MENSUELLES. */
    long countByPharmacopeeIdAndStatutAndDateVenteBetween(Long pharmacopeeId, StatutVente statut,
                                                          LocalDateTime debut, LocalDateTime fin);
}
