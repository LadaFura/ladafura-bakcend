package com.pharmacopee.ladafura.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.LigneAchat;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {

    @EntityGraph(attributePaths = "produit")
    List<LigneAchat> findByAchatId(Long achatId);
}
