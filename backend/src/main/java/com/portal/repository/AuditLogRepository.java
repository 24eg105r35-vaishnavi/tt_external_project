package com.portal.repository;

import com.portal.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AuditLog a " +
           "WHERE (:action IS NULL OR :action = '' OR a.action = :action) " +
           "AND (:performedBy IS NULL OR :performedBy = '' OR LOWER(a.performedBy) LIKE LOWER(CONCAT('%', :performedBy, '%'))) " +
           "ORDER BY a.createdAt DESC")
    org.springframework.data.domain.Page<AuditLog> searchAuditLogs(
            @org.springframework.data.repository.query.Param("action") String action,
            @org.springframework.data.repository.query.Param("performedBy") String performedBy,
            org.springframework.data.domain.Pageable pageable);
}
