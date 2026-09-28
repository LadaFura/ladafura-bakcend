package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.AgentCollecte;

@Repository
public interface AgentCollecteRepository extends JpaRepository<AgentCollecte, Long> {

    Optional<AgentCollecte> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

    Optional<AgentCollecte> findByFirebaseUid(String firebaseUid);

    Optional<AgentCollecte> findByEmail(String email);
}
