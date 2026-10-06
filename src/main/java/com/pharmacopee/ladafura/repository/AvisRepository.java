package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Avis;
import com.pharmacopee.ladafura.enums.StatutAvis;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {

    List<Avis> findByPharmacopeeId(Long pharmacopeeId);

    Page<Avis> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    List<Avis> findByUtilisateurId(Long utilisateurId);

    Page<Avis> findByUtilisateurId(Long utilisateurId, Pageable pageable);

    Page<Avis> findByUtilisateurIdAndStatut(Long utilisateurId, StatutAvis statut, Pageable pageable);

    java.util.Optional<Avis> findByIdAndUtilisateurId(Long id, Long utilisateurId);

    boolean existsByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    java.util.Optional<Avis> findByUtilisateurIdAndPharmacopeeId(Long utilisateurId, Long pharmacopeeId);

    List<Avis> findByStatut(StatutAvis statut);

    Page<Avis> findByStatut(StatutAvis statut, Pageable pageable);

    long countByStatut(StatutAvis statut);

    long countByPharmacopeeId(Long pharmacopeeId);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.pharmacopee.id = :pharmacopeeId AND a.statut = :statut")
    Double findAverageNoteByPharmacopeeIdAndStatut(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut);

    Page<Avis> findByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutAvis statut, Pageable pageable);

    long countByPharmacopeeIdAndStatut(Long pharmacopeeId, StatutAvis statut);

    java.util.Optional<Avis> findByIdAndPharmacopeeIdAndStatut(Long id, Long pharmacopeeId, StatutAvis statut);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.pharmacopee.id = :pharmacopeeId AND a.statut = :statut AND a.note = :note")
    long countByPharmacopeeIdAndStatutAndNote(@Param("pharmacopeeId") Long pharmacopeeId, @Param("statut") StatutAvis statut, @Param("note") Integer note);
}
