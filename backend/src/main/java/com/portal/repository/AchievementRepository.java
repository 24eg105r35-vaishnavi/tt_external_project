package com.portal.repository;

import com.portal.entity.Achievement;
import com.portal.entity.Status;
import com.portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByStudent(Student student);
    List<Achievement> findByStudentOrderByCreatedAtDesc(Student student);
    List<Achievement> findByStatus(Status status);
    List<Achievement> findByStudentAndStatus(Student student, Status status);
}
