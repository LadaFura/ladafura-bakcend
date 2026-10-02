package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Praticien;

@Repository
public interface PraticienRepository extends JpaRepository<Praticien, Long> {

    Optional<Praticien> findByEmail(String email);

    Optional<Praticien> findByFirebaseUid(String firebaseUid);
}
