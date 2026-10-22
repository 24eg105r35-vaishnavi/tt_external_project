package com.portal.repository;

import com.portal.entity.Achievement;
import com.portal.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    List<Verification> findByAchievement(Achievement achievement);
}
