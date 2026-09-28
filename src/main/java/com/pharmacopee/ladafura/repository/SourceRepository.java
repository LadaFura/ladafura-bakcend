package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Source;
import com.pharmacopee.ladafura.enums.Role;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findBySpecialiteContainingIgnoreCase(String specialite);

    @Query("SELECT s FROM Source s WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(s.nom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.prenom) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.specialite) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.adresse) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "s.telephone LIKE CONCAT('%', :query, '%')) AND " +
           "(:role IS NULL OR s.role = :role)")
    Page<Source> searchSources(@Param("query") String query, @Param("role") Role role, Pageable pageable);

    List<Source> findByTelephone(String telephone);

    List<Source> findByRole(Role role);
}
