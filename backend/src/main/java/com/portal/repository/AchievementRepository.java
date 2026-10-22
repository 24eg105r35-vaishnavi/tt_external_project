package com.portal.repository;

import com.portal.entity.Achievement;
import com.portal.entity.Status;
import com.portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByStudent(Student student);
    List<Achievement> findByStudentOrderByCreatedAtDesc(Student student);
    List<Achievement> findByStatus(Status status);
    List<Achievement> findByStudentAndStatus(Student student, Status status);

    // query pending achievements for faculty queue with department routing and optional filters
    @Query("SELECT a FROM Achievement a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user u " +
           "JOIN FETCH a.category c " +
           "WHERE a.status = com.portal.entity.Status.PENDING " +
           "AND (c.verifierDepartment = 'ALL' OR LOWER(c.verifierDepartment) = LOWER(:facultyDept)) " +
           "AND (:branch IS NULL OR :branch = '' OR s.branch = :branch) " +
           "AND (:year IS NULL OR s.year = :year) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "ORDER BY a.createdAt ASC")
    List<Achievement> findPendingForFacultyQueue(@Param("facultyDept") String facultyDept,
                                                @Param("branch") String branch,
                                                @Param("year") Integer year,
                                                @Param("categoryId") Long categoryId);

    // query pending achievements with optional branch, year, category filters
    @Query("SELECT a FROM Achievement a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user u " +
           "JOIN FETCH a.category c " +
           "WHERE a.status = com.portal.entity.Status.PENDING " +
           "AND (:branch IS NULL OR :branch = '' OR s.branch = :branch) " +
           "AND (:year IS NULL OR s.year = :year) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "ORDER BY a.createdAt ASC")
    List<Achievement> findPendingAchievements(@Param("branch") String branch,
                                             @Param("year") Integer year,
                                             @Param("categoryId") Long categoryId);
}

