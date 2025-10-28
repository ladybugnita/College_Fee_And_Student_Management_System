package com.example.collegefeeandstudentmanagement.repository;
import com.example.collegefeeandstudentmanagement.entity.MiscExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MiscExpenseRepository extends JpaRepository<MiscExpense, Long> {
    List<MiscExpense> findByStudentFeeId(Long studentFeeId);
}
