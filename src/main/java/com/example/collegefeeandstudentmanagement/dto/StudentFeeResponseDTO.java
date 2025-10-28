package com.example.collegefeeandstudentmanagement.dto;

import com.example.collegefeeandstudentmanagement.entity.MiscExpense;
import com.example.collegefeeandstudentmanagement.entity.StudentFee;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudentFeeResponseDTO {
    private Long id;
    private Long studentId;
    private BigDecimal totalFee;
    private BigDecimal scholarshipAmount;
    private BigDecimal discountAmount;
    private BigDecimal netFee;
    private BigDecimal admissionFee;
    private BigDecimal tuitionFee;
    private BigDecimal examFee;
    private BigDecimal universityCharge;
    private BigDecimal ecaCharge;
    private Integer courseDurationYears;
    private Integer totalInstallments;
    private List<InstallmentDTO> installments;
    private List<MiscExpenseDTO> miscExpenses;


    public StudentFeeResponseDTO() {
    }

    public StudentFeeResponseDTO(Long id, Long studentId, BigDecimal totalFee,
                                 BigDecimal scholarshipAmount, BigDecimal discountAmount,
                                 BigDecimal netFee, Integer courseDurationYears,
                                 Integer totalInstallments, List<InstallmentDTO> installments, BigDecimal admissionFee, BigDecimal tuitionFee,
                                 BigDecimal examFee, BigDecimal universityCharge, BigDecimal ecaCharge, List<MiscExpenseDTO> miscExpenses) {
        this.id = id;
        this.studentId = studentId;
        this.totalFee = totalFee;
        this.scholarshipAmount = scholarshipAmount;
        this.discountAmount = discountAmount;
        this.netFee = netFee;
        this.courseDurationYears = courseDurationYears;
        this.totalInstallments = totalInstallments;
        this.installments = installments;
        this.admissionFee = admissionFee;
        this.tuitionFee = tuitionFee;
        this.examFee = examFee;
        this.universityCharge = universityCharge;
        this.ecaCharge = ecaCharge;
        this.miscExpenses = miscExpenses;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public BigDecimal getScholarshipAmount() {
        return scholarshipAmount;
    }

    public void setScholarshipAmount(BigDecimal scholarshipAmount) {
        this.scholarshipAmount = scholarshipAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetFee() {
        return netFee;
    }

    public void setNetFee(BigDecimal netFee) {
        this.netFee = netFee;
    }

    public Integer getCourseDurationYears() {
        return courseDurationYears;
    }

    public void setCourseDurationYears(Integer courseDurationYears) {
        this.courseDurationYears = courseDurationYears;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public List<InstallmentDTO> getInstallments() {
        return installments;
    }

    public void setInstallments(List<InstallmentDTO> installments) {
        this.installments = installments;
    }

    public BigDecimal getAdmissionFee() {
        return admissionFee;
    }

    public void setAdmissionFee(BigDecimal admissionFee) {
        this.admissionFee = admissionFee;
    }

    public BigDecimal getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(BigDecimal tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public BigDecimal getExamFee() {
        return examFee;
    }

    public void setExamFee(BigDecimal examFee) {
        this.examFee = examFee;
    }

    public BigDecimal getUniversityCharge() {
        return universityCharge;
    }

    public void setUniversityCharge(BigDecimal universityCharge) {
        this.universityCharge = universityCharge;
    }

    public BigDecimal getEcaCharge() {
        return ecaCharge;
    }

    public void setEcaCharge(BigDecimal ecaCharge) {
        this.ecaCharge = ecaCharge;
    }
    public List<MiscExpenseDTO> getMiscExpenses(){
        return miscExpenses;
    }
    public void setMiscExpenses(List<MiscExpenseDTO> miscExpenses){
        this.miscExpenses = miscExpenses;
    }

    public static StudentFeeResponseDTO fromEntity(StudentFee fee) {
        StudentFeeResponseDTO dto = new StudentFeeResponseDTO();
        dto.setId(fee.getId());
        dto.setStudentId(fee.getStudent().getId());
        dto.setTotalFee(fee.getTotalFee());
        dto.setScholarshipAmount(fee.getScholarshipAmount());
        dto.setDiscountAmount(fee.getDiscountAmount());
        dto.setNetFee(fee.getNetFee());
        dto.setAdmissionFee(fee.getAdmissionFee());
        dto.setTuitionFee(fee.getTuitionFee());
        dto.setExamFee(fee.getExamFee());
        dto.setUniversityCharge(fee.getUniversityCharge());
        dto.setEcaCharge(fee.getEcaCharge());
        dto.setCourseDurationYears(fee.getCourseDurationYears());
        dto.setTotalInstallments(fee.getTotalInstallments());

        if (fee.getMiscExpenses() != null) {
            List<MiscExpenseDTO> miscExpenseDTOs = fee.getMiscExpenses().stream()
                    .map(miscExpense -> {
                        MiscExpenseDTO miscDTO = new MiscExpenseDTO();
                        miscDTO.setId(miscExpense.getId());
                        miscDTO.setAmount(miscExpense.getAmount());
                        miscDTO.setDescription(miscExpense.getDescription());
                        miscDTO.setCreatedAt(miscExpense.getCreatedAt()); // Make sure this field exists in MiscExpense entity
                        return miscDTO;
                    })
                    .collect(Collectors.toList());
            dto.setMiscExpenses(miscExpenseDTOs);
        } else {
            dto.setMiscExpenses(new ArrayList<>());
        }

        if (fee.getInstallments() != null) {
            List<InstallmentDTO> installmentDTOs = fee.getInstallments().stream()
                    .map(installment -> {
                        InstallmentDTO installmentDTO = new InstallmentDTO();
                        installmentDTO.setId(installment.getId());
                        installmentDTO.setInstallmentNumber(installment.getInstallmentNumber());
                        installmentDTO.setAmount(installment.getAmount());
                        installmentDTO.setPaidAmount(installment.getPaidAmount());
                        installmentDTO.setPendingAmount(installment.getPendingAmount());
                        installmentDTO.setPaid(installment.isPaid());
                        installmentDTO.setDescription(installment.getDescription());
                        return installmentDTO;
                    })
                    .collect(Collectors.toList());
            dto.setInstallments(installmentDTOs);
        } else {
            dto.setInstallments(new ArrayList<>());
        }
        return dto;
    }
}
