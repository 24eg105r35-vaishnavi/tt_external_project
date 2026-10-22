package com.portal.repository;

import com.portal.entity.Student;
import com.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByProfileSlug(String profileSlug);
    Optional<Student> findByUser(User user);

    // Part 8: find students with PUBLIC profile and at least one APPROVED achievement for sitemap
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM Student s " +
           "JOIN Achievement a ON a.student = s " +
           "WHERE s.visibility = com.portal.entity.Visibility.PUBLIC " +
           "AND a.status = com.portal.entity.Status.APPROVED")
    java.util.List<Student> findPublicStudentsWithApprovedAchievements();
}
