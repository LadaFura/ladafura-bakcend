package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.FichierMediaStocke;

@Repository
public interface FichierMediaStockeRepository extends JpaRepository<FichierMediaStocke, Long> {

    Optional<FichierMediaStocke> findByChemin(String chemin);

    boolean existsByChemin(String chemin);

    void deleteByChemin(String chemin);
}
