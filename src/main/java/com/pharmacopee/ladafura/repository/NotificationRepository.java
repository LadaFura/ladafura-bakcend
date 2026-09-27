package com.pharmacopee.ladafura.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pharmacopee.ladafura.Models.Notification;
import com.pharmacopee.ladafura.enums.TypeNotification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByPharmacopeeId(Long pharmacopeeId, Pageable pageable);

    Page<Notification> findByPharmacopeeIdAndLue(Long pharmacopeeId, Boolean lue, Pageable pageable);

    Page<Notification> findByPharmacopeeIdAndType(Long pharmacopeeId, TypeNotification type, Pageable pageable);

    Page<Notification> findByPharmacopeeIdAndLueAndType(Long pharmacopeeId, Boolean lue, TypeNotification type, Pageable pageable);

    Optional<Notification> findByIdAndPharmacopeeId(Long id, Long pharmacopeeId);

    long countByPharmacopeeId(Long pharmacopeeId);

    long countByPharmacopeeIdAndLueFalse(Long pharmacopeeId);

    long countByPharmacopeeIdAndType(Long pharmacopeeId, TypeNotification type);

    long countByPharmacopeeIdAndLueFalseAndType(Long pharmacopeeId, TypeNotification type);

    @Modifying
    @Query("UPDATE Notification n SET n.lue = true, n.dateLecture = CURRENT_TIMESTAMP WHERE n.pharmacopee.id = :pharmacopeeId AND n.lue = false")
    void markAllAsReadByPharmacopeeId(@Param("pharmacopeeId") Long pharmacopeeId);
}
