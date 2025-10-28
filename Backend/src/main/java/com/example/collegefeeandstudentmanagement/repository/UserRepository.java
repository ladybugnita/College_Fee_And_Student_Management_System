package com.example.collegefeeandstudentmanagement.repository;

import com.example.collegefeeandstudentmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    default Optional<User> findByEmail(String email) {
        return findByUsername(email);
    }
}
