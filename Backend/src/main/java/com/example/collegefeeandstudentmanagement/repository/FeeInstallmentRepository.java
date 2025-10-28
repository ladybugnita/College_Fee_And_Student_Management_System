package com.example.collegefeeandstudentmanagement.repository;

import com.example.collegefeeandstudentmanagement.entity.FeeInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface FeeInstallmentRepository extends JpaRepository<FeeInstallment, Long>{
 Optional <FeeInstallment> findByStudentFee_Student_IdAndInstallmentNumber(Long studentId,int installmentNumber);
 List <FeeInstallment> findByStudentFee_Student_Id(Long studentId);
 List<FeeInstallment> findByStudentFeeId(Long studentFeeId);
 @Modifying
 @Query("DELETE FROM FeeInstallment i where i.studentFee.id = :studentFeeId")
 void deleteByStudentFeeId(@Param("studentFeeId")Long studentFeeId);
}
