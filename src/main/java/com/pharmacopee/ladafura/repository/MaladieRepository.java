package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Maladie;

@Repository
public interface MaladieRepository extends JpaRepository<Maladie, Long> {

    Optional<Maladie> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    List<Maladie> findByNomContainingIgnoreCase(String keyword);

    Page<Maladie> findByNomContainingIgnoreCase(String keyword, Pageable pageable);
}
