package com.skybooker.auth.repository;

import com.skybooker.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

     // Check if any user exists with a given role.
    boolean existsByRole(String role);

    // Count number of users with a specific role.
    long countByRole(String role);
}