package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.AgentCollecte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentCollecteRepository extends JpaRepository<AgentCollecte, Long> {

    Optional<AgentCollecte> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);
}
