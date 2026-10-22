package com.portal.repository;

import com.portal.entity.Achievement;
import com.portal.entity.Document;
import com.portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByAchievement(Achievement achievement);

    @Query("SELECT COUNT(d) > 0 FROM Document d WHERE d.checksum = :checksum AND d.achievement.student = :student")
    boolean existsByChecksumAndStudent(@Param("checksum") String checksum, @Param("student") Student student);
}
