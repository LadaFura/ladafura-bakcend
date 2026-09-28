package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Pharmacopee;
import com.pharmacopee.ladafura.enums.StatutPharmacopee;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PharmacopeeRepository extends JpaRepository<Pharmacopee, Long> {

    List<Pharmacopee> findByStatut(StatutPharmacopee statut);

    Page<Pharmacopee> findByStatut(StatutPharmacopee statut, Pageable pageable);

    Optional<Pharmacopee> findByUtilisateurId(Long utilisateurId);

    Optional<Pharmacopee> findByUtilisateurFirebaseUid(String firebaseUid);

    Optional<Pharmacopee> findByUtilisateurEmail(String email);

    long countByStatut(StatutPharmacopee statut);

    List<Pharmacopee> findByNomContainingIgnoreCase(String keyword);

    Page<Pharmacopee> findByNomContainingIgnoreCase(String keyword, Pageable pageable);

    @Query("SELECT p FROM Pharmacopee p LEFT JOIN p.localisation l WHERE p.statut = :statut " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:region IS NULL OR :region = '' OR LOWER(l.region) LIKE LOWER(CONCAT('%', :region, '%'))) " +
           "AND (:cercle IS NULL OR :cercle = '' OR LOWER(l.cercle) LIKE LOWER(CONCAT('%', :cercle, '%'))) " +
           "AND (:commune IS NULL OR :commune = '' OR LOWER(l.commune) LIKE LOWER(CONCAT('%', :commune, '%')))")
    Page<Pharmacopee> searchPharmacopees(@Param("statut") StatutPharmacopee statut,
                                         @Param("keyword") String keyword,
                                         @Param("region") String region,
                                         @Param("cercle") String cercle,
                                         @Param("commune") String commune,
                                         Pageable pageable);

    @Query("SELECT p FROM Pharmacopee p LEFT JOIN p.localisation l WHERE p.statut = :statut " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Pharmacopee> searchTopByStatutAndKeyword(@Param("statut") StatutPharmacopee statut,
                                                 @Param("keyword") String keyword,
                                                 Pageable pageable);
}
