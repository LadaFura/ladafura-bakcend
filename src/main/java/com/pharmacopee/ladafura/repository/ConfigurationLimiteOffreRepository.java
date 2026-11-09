package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.ConfigurationLimiteOffre;

@Repository
public interface ConfigurationLimiteOffreRepository extends JpaRepository<ConfigurationLimiteOffre, Long> {

    @EntityGraph(attributePaths = "limite")
    List<ConfigurationLimiteOffre> findByOffreId(Long offreId);

    Optional<ConfigurationLimiteOffre> findByOffreIdAndLimiteId(Long offreId, Long limiteId);
}
