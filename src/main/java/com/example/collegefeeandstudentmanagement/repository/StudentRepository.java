package com.example.collegefeeandstudentmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.collegefeeandstudentmanagement.entity.Student;
import java.util.Optional;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    List<Student> findByStudentFeeIsNotNull();
}
