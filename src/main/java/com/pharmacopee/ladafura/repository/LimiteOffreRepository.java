package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LimiteOffre;

@Repository
public interface LimiteOffreRepository extends JpaRepository<LimiteOffre, Long> {

    Optional<LimiteOffre> findByCodeLimite(String codeLimite);
}
