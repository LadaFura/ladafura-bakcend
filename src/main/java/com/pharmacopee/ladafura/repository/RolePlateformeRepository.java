package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.RolePlateforme;

@Repository
public interface RolePlateformeRepository extends JpaRepository<RolePlateforme, Long> {

    Optional<RolePlateforme> findByCode(String code);

    boolean existsByCode(String code);
}
