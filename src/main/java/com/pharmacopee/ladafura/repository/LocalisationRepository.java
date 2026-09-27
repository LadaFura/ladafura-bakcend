package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.Localisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalisationRepository extends JpaRepository<Localisation, Long> {

    List<Localisation> findByRegionIgnoreCase(String region);

    List<Localisation> findByCercleIgnoreCase(String cercle);

    List<Localisation> findByCommuneIgnoreCase(String commune);
}
