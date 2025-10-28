package com.example.collegefeeandstudentmanagement.service;

import com.example.collegefeeandstudentmanagement.dto.InstallmentDTO;
import com.example.collegefeeandstudentmanagement.dto.MiscExpenseDTO;
import com.example.collegefeeandstudentmanagement.entity.MiscExpense;
import com.example.collegefeeandstudentmanagement.dto.StudentFeeResponseDTO;
import com.example.collegefeeandstudentmanagement.entity.FeeInstallment;
import com.example.collegefeeandstudentmanagement.entity.StudentFee;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentFeeMapperService {

    public StudentFeeResponseDTO toDTO(StudentFee studentFee) {
        if (studentFee == null) {
            return null;
        }

        StudentFeeResponseDTO dto = new StudentFeeResponseDTO();
        dto.setId(studentFee.getId());
        dto.setStudentId(studentFee.getStudent() != null ? studentFee.getStudent().getId() : null);
        dto.setTuitionFee(studentFee.getTuitionFee());
        dto.setExamFee(studentFee.getExamFee());
        dto.setEcaCharge(studentFee.getEcaCharge());
        dto.setAdmissionFee(studentFee.getAdmissionFee());
        dto.setUniversityCharge(studentFee.getUniversityCharge());
        dto.setScholarshipAmount(studentFee.getScholarshipAmount());
        dto.setDiscountAmount(studentFee.getDiscountAmount());
        dto.setTotalFee(studentFee.getTotalFee());
        dto.setNetFee(studentFee.getNetFee());
        dto.setCourseDurationYears(studentFee.getCourseDurationYears());
        dto.setTotalInstallments(studentFee.getTotalInstallments());

        if (studentFee.getInstallments() != null) {
            List<InstallmentDTO> installmentDTOs = studentFee.getInstallments().stream()
                    .map(this::mapToInstallmentDTO)
                    .collect(Collectors.toList());
            dto.setInstallments(installmentDTOs);
        }

        if (studentFee.getMiscExpenses() != null) {
            List<MiscExpenseDTO> miscExpenseDTOs = studentFee.getMiscExpenses().stream()
                    .map(this::mapToMiscExpenseDTO)
                    .collect(Collectors.toList());
            dto.setMiscExpenses(miscExpenseDTOs);
        }

        return dto;
    }

    private InstallmentDTO mapToInstallmentDTO(FeeInstallment installment) {
        if (installment == null) return null;

        InstallmentDTO dto = new InstallmentDTO();
        dto.setId(installment.getId());
        dto.setInstallmentNumber(installment.getInstallmentNumber());
        dto.setAmount(installment.getAmount());
        dto.setPaidAmount(installment.getPaidAmount());
        dto.setPendingAmount(installment.getPendingAmount());
        dto.setPaid(installment.isPaid());
        dto.setDescription(installment.getDescription());
        return dto;
    }
    private MiscExpenseDTO mapToMiscExpenseDTO(MiscExpense miscExpense) {
        if (miscExpense == null) return null;

        MiscExpenseDTO dto = new MiscExpenseDTO();
        dto.setId(miscExpense.getId());
        dto.setAmount(miscExpense.getAmount());
        dto.setDescription(miscExpense.getDescription());
        dto.setCreatedAt(miscExpense.getCreatedAt());
        return dto;
    }
    public StudentFeeResponseDTO toResponseDTO(StudentFee studentFee) {
        return toDTO(studentFee);
    }
}
