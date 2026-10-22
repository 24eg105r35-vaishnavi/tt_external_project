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
    List<Achievement> findByStudentAndStatusOrderByEventDateDesc(Student student, Status status);

    // eagerly fetch category to prevent LazyInitializationException on public profile
    @Query("SELECT a FROM Achievement a " +
           "JOIN FETCH a.category c " +
           "WHERE a.student = :student AND a.status = :status " +
           "ORDER BY a.eventDate DESC")
    List<Achievement> findApprovedAchievementsForProfile(@Param("student") Student student,
                                                        @Param("status") Status status);

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

    // Part 7: search approved achievements with filters and paging
    @Query(value = "SELECT a FROM Achievement a " +
                   "JOIN FETCH a.student s " +
                   "JOIN FETCH s.user u " +
                   "JOIN FETCH a.category c " +
                   "WHERE a.status = com.portal.entity.Status.APPROVED " +
                   "AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "AND (:branch IS NULL OR s.branch = :branch) " +
                   "AND (:year IS NULL OR s.year = :year) " +
                   "AND (:categoryId IS NULL OR c.id = :categoryId) " +
                   "AND (:level IS NULL OR a.level = :level) " +
                   "AND (:fromDate IS NULL OR a.eventDate >= :fromDate) " +
                   "AND (:toDate IS NULL OR a.eventDate <= :toDate)",
           countQuery = "SELECT COUNT(a) FROM Achievement a " +
                   "JOIN a.student s " +
                   "JOIN s.user u " +
                   "JOIN a.category c " +
                   "WHERE a.status = com.portal.entity.Status.APPROVED " +
                   "AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                   "AND (:branch IS NULL OR s.branch = :branch) " +
                   "AND (:year IS NULL OR s.year = :year) " +
                   "AND (:categoryId IS NULL OR c.id = :categoryId) " +
                   "AND (:level IS NULL OR a.level = :level) " +
                   "AND (:fromDate IS NULL OR a.eventDate >= :fromDate) " +
                   "AND (:toDate IS NULL OR a.eventDate <= :toDate)")
    org.springframework.data.domain.Page<Achievement> searchApprovedAchievements(
            @Param("keyword") String keyword,
            @Param("branch") String branch,
            @Param("year") Integer year,
            @Param("categoryId") Long categoryId,
            @Param("level") String level,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable);

    // Part 7: export approved achievements with filters (capped via Pageable)
    @Query("SELECT a FROM Achievement a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user u " +
           "JOIN FETCH a.category c " +
           "WHERE a.status = com.portal.entity.Status.APPROVED " +
           "AND (:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:branch IS NULL OR s.branch = :branch) " +
           "AND (:year IS NULL OR s.year = :year) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:level IS NULL OR a.level = :level) " +
           "AND (:fromDate IS NULL OR a.eventDate >= :fromDate) " +
           "AND (:toDate IS NULL OR a.eventDate <= :toDate) " +
           "ORDER BY a.eventDate DESC")
    List<Achievement> exportApprovedAchievements(
            @Param("keyword") String keyword,
            @Param("branch") String branch,
            @Param("year") Integer year,
            @Param("categoryId") Long categoryId,
            @Param("level") String level,
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable);
}

