package com.example.blogws.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.blogws.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    // Check if email already exists
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
}
