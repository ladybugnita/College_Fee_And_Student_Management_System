package com.example.collegefeeandstudentmanagement.controller;

import com.example.collegefeeandstudentmanagement.dto.AssignFeeRequest;
import com.example.collegefeeandstudentmanagement.dto.StudentFeeResponseDTO;
import com.example.collegefeeandstudentmanagement.entity.Student;
import com.example.collegefeeandstudentmanagement.repository.StudentFeeRepository;
import com.example.collegefeeandstudentmanagement.repository.FeeInstallmentRepository;
import com.example.collegefeeandstudentmanagement.service.StudentFeeMapperService;
import com.example.collegefeeandstudentmanagement.service.StudentFeeService;
import com.example.collegefeeandstudentmanagement.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/studentfee")
public class StudentFeeController {

    private final StudentFeeService feeService;
    private final StudentRepository studentRepository;
    private final StudentFeeMapperService mapperService;
    private final StudentFeeRepository feeRepository;
    private final FeeInstallmentRepository installmentRepository;

    public StudentFeeController(StudentFeeService feeService, StudentRepository studentRepository, StudentFeeMapperService mapperService, StudentFeeRepository feeRepository, FeeInstallmentRepository installmentRepository) {
        this.feeService = feeService;
        this.studentRepository = studentRepository;
        this.mapperService = mapperService;
        this.feeRepository = feeRepository;
        this.installmentRepository = installmentRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign/{studentId}")
    public ResponseEntity<StudentFeeResponseDTO> assignFee(
            @PathVariable Long studentId,
            @RequestBody AssignFeeRequest request
    ) {
        return studentRepository.findById(studentId)
                .map(student -> {
                    if (student.getStudentFee() != null) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<StudentFeeResponseDTO>build();
                    }
                    int years = request.getYears() != null ? request.getYears() : 0;
                    int totalInstallments = request.getTotalInstallments() != null ? request.getTotalInstallments() : 0;

                    return feeService.assignFee(studentId, request.getTuitionFee(), request.getExamFee(), request.getEcaCharge(), request.getAdmissionFee(), request.getUniversityCharge(), request.getScholarship(),
                                    request.getDiscount(), years, totalInstallments)
                            .map(ResponseEntity::ok)
                            .orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/assign/{studentId}")
    public ResponseEntity<StudentFeeResponseDTO> getAssignedFee(@PathVariable Long studentId, Authentication auth) {

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();

        if(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))
                && !student.getEmail().equals(auth.getName())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return feeService.getAssignedFee(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/assign/{studentId}")
    public ResponseEntity<?> updateStudentFee(@PathVariable Long studentId, @RequestBody UpdateFeeRequest request) {
        try{
            Optional<StudentFeeResponseDTO> updatedFee = feeService.updateFee(
                    studentId,
                    request.getAdmissionFee(),
                    request.getTuitionFee(),
                    request.getExamFee(),
                    request.getUniversityCharge(),
                    request.getEcaCharge(),
                    request.getScholarship(),
                    request.getDiscount(),
                    request.getYears(),
                    request.getTotalInstallments()
            );
            if (updatedFee.isPresent()) {
                return ResponseEntity.ok(updatedFee.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/assign/{studentId}")
    public ResponseEntity<StudentFeeResponseDTO> patchFee(@PathVariable Long studentId, @RequestBody Map<String,Object> updates) {
        try {
            BigDecimal admissionFee = getBigDecimalFromMap(updates, "admissionFee");
            BigDecimal tuitionFee = getBigDecimalFromMap(updates, "tuitionFee");
            BigDecimal examFee = getBigDecimalFromMap(updates, "examFee");
            BigDecimal universityCharge = getBigDecimalFromMap(updates, "universityCharge");
            BigDecimal ecaCharge = getBigDecimalFromMap(updates, "ecaCharge");
            BigDecimal scholarship = getBigDecimalFromMap(updates, "scholarship");
            BigDecimal discount = getBigDecimalFromMap(updates, "discount");
            Integer years = getIntegerFromMap(updates, "years");
            Integer totalInstallments = getIntegerFromMap(updates, "totalInstallments");

            Optional<StudentFeeResponseDTO> updatedFee = feeService.patchFee(
                    studentId, admissionFee, tuitionFee, examFee, universityCharge, ecaCharge,
                    scholarship, discount, years, totalInstallments
            );

            return updatedFee.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private BigDecimal getBigDecimalFromMap(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            if (map.get(key) instanceof BigDecimal) {
                return (BigDecimal) map.get(key);
            } else if (map.get(key) instanceof Number) {
                return BigDecimal.valueOf(((Number) map.get(key)).doubleValue());
            } else if (map.get(key) instanceof String) {
                return new BigDecimal((String) map.get(key));
            }
        }
        return null;
    }

    private Integer getIntegerFromMap(Map<String, Object> map, String key) {
        if (map.containsKey(key) && map.get(key) != null) {
            if (map.get(key) instanceof Integer) {
                return (Integer) map.get(key);
            } else if (map.get(key) instanceof Number) {
                return ((Number) map.get(key)).intValue();
            } else if (map.get(key) instanceof String) {
                return Integer.parseInt((String) map.get(key));
            }
        }
        return null;
    }

    public static class UpdateFeeRequest {
        private BigDecimal admissionFee;
        private BigDecimal tuitionFee;
        private BigDecimal examFee;
        private BigDecimal universityCharge;
        private BigDecimal ecaCharge;
        private BigDecimal scholarship;
        private BigDecimal discount;
        private Integer years;
        private Integer totalInstallments;

        public BigDecimal getAdmissionFee() { return admissionFee; }
        public void setAdmissionFee(BigDecimal admissionFee) { this.admissionFee = admissionFee; }
        public BigDecimal getTuitionFee() { return tuitionFee; }
        public void setTuitionFee(BigDecimal tuitionFee) { this.tuitionFee = tuitionFee; }
        public BigDecimal getExamFee() { return examFee; }
        public void setExamFee(BigDecimal examFee) { this.examFee = examFee; }
        public BigDecimal getUniversityCharge() { return universityCharge; }
        public void setUniversityCharge(BigDecimal universityCharge) { this.universityCharge = universityCharge; }
        public BigDecimal getEcaCharge() { return ecaCharge; }
        public void setEcaCharge(BigDecimal ecaCharge) { this.ecaCharge = ecaCharge; }
        public BigDecimal getScholarship() { return scholarship; }
        public void setScholarship(BigDecimal scholarship) { this.scholarship = scholarship; }
        public BigDecimal getDiscount() { return discount; }
        public void setDiscount(BigDecimal discount) { this.discount = discount; }
        public Integer getYears() { return years; }
        public void setYears(Integer years) { this.years = years; }
        public Integer getTotalInstallments() { return totalInstallments; }
        public void setTotalInstallments(Integer totalInstallments) { this.totalInstallments = totalInstallments; }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/assign/{studentId}")
    public ResponseEntity<Void> deleteFee(@PathVariable Long studentId) {
        if (feeService.deleteFee(studentId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{studentId}/misc")
    public ResponseEntity<?> addMiscExpense(@PathVariable Long studentId, @RequestBody MiscExpenseRequest request,Authentication auth){
        try{
            feeService.addMiscExpense(studentId, request.getAmount(), request.getDescription());
            StudentFeeResponseDTO dto = feeService.getAssignedFee(studentId)
                    .orElseThrow(() -> new RuntimeException("Student fee not found"));
            return ResponseEntity.ok(dto);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }
    public static class MiscExpenseRequest{
        private BigDecimal amount;
        private String description;
        public BigDecimal getAmount(){
            return amount;
        }
        public void setAmount(BigDecimal amount){
            this.amount = amount;
        }
        public String getDescription(){
            return description;
        }
        public void setDescription(String description){
            this.description = description;
        }
    }
}
