package com.pharmacopee.ladafura.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Utilisateur;
import com.pharmacopee.ladafura.enums.Role;
import com.pharmacopee.ladafura.enums.StatutUtilisateur;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByFirebaseUid(String firebaseUid);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByStatut(StatutUtilisateur statut);

    Page<Utilisateur> findByRole(Role role, Pageable pageable);

    Page<Utilisateur> findByStatut(StatutUtilisateur statut, Pageable pageable);

    Page<Utilisateur> findByRoleAndStatut(Role role, StatutUtilisateur statut, Pageable pageable);

    long countByRole(Role role);

    long countByStatut(StatutUtilisateur statut);
}
