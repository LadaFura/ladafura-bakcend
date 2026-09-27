package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findBySpecialiteContainingIgnoreCase(String specialite);
}
