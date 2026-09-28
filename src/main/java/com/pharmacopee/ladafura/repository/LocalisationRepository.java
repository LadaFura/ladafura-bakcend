package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Localisation;

@Repository
public interface LocalisationRepository extends JpaRepository<Localisation, Long> {

    List<Localisation> findByRegionIgnoreCase(String region);

    List<Localisation> findByCercleIgnoreCase(String cercle);

    List<Localisation> findByCommuneIgnoreCase(String commune);

    List<Localisation> findByRegionIgnoreCaseAndCercleIgnoreCaseAndCommuneIgnoreCaseAndLocaliteIgnoreCase(
            String region, String cercle, String commune, String localite);

    @Query("SELECT l FROM Localisation l WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(l.region) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.cercle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.commune) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.localite) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Localisation> searchLocalisations(@Param("query") String query, Pageable pageable);
}
