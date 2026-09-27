package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Source;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findBySpecialiteContainingIgnoreCase(String specialite);
}
