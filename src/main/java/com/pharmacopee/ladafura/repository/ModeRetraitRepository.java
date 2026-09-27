package com.pharmacopee.ladafura.repository;

import com.pharmacopee.ladafura.Models.ModeRetrait;
import com.pharmacopee.ladafura.enums.TypeModeRetrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModeRetraitRepository extends JpaRepository<ModeRetrait, Long> {

    List<ModeRetrait> findByPharmacopeeId(Long pharmacopeeId);

    Optional<ModeRetrait> findByPharmacopeeIdAndType(Long pharmacopeeId, TypeModeRetrait type);
}
