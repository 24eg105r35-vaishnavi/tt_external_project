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
}
