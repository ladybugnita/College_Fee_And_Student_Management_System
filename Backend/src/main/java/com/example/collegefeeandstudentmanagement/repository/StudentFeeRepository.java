package com.example.collegefeeandstudentmanagement.repository;

import com.example.collegefeeandstudentmanagement.entity.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {
    Optional<StudentFee> findByStudentId(Long studentId);
}
