package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.ParametrePlateforme;

@Repository
public interface ParametrePlateformeRepository extends JpaRepository<ParametrePlateforme, Long> {

    Optional<ParametrePlateforme> findByCode(String code);
}
