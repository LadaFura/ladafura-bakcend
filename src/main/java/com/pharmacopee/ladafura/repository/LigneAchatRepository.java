package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LigneAchat;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    @EntityGraph(attributePaths = "produit")
    List<LigneAchat> findByAchatId(Long achatId);

    /** Produits commandés auprès d'un fournisseur : lignes de ses achats validés. */
    @Query(value = """
           select l from LigneAchat l join fetch l.achat a join fetch l.produit
           where a.fournisseur.id = :fournisseurId and a.pharmacopee.id = :pharmacopeeId
             and a.statut = com.pharmacopee.ladafura.enums.StatutAchat.VALIDE
           order by a.dateAchat desc, l.id desc
           """,
           countQuery = """
           select count(l) from LigneAchat l
           where l.achat.fournisseur.id = :fournisseurId and l.achat.pharmacopee.id = :pharmacopeeId
             and l.achat.statut = com.pharmacopee.ladafura.enums.StatutAchat.VALIDE
           """)
    Page<LigneAchat> historiqueFournisseur(@Param("pharmacopeeId") Long pharmacopeeId, @Param("fournisseurId") Long fournisseurId, Pageable pageable);
}
