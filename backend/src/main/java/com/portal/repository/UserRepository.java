package com.portal.repository;

import com.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE (:keyword IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    org.springframework.data.domain.Page<User> searchUsers(@org.springframework.data.repository.query.Param("keyword") String keyword,
                                                           org.springframework.data.domain.Pageable pageable);
}
