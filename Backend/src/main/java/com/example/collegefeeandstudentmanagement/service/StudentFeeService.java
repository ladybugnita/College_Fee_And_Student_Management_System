package com.example.collegefeeandstudentmanagement.service;

import com.example.collegefeeandstudentmanagement.entity.FeeInstallment;
import com.example.collegefeeandstudentmanagement.dto.StudentFeeResponseDTO;
import com.example.collegefeeandstudentmanagement.entity.Student;
import com.example.collegefeeandstudentmanagement.entity.StudentFee;
import com.example.collegefeeandstudentmanagement.entity.MiscExpense;
import com.example.collegefeeandstudentmanagement.repository.MiscExpenseRepository;
import com.example.collegefeeandstudentmanagement.repository.FeeInstallmentRepository;
import com.example.collegefeeandstudentmanagement.repository.StudentFeeRepository;
import com.example.collegefeeandstudentmanagement.repository.StudentRepository;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
public class StudentFeeService {
    private final StudentFeeRepository feeRepository;
    private final StudentRepository studentRepository;
    private final FeeInstallmentRepository installmentRepository;
    private final EsewaPaymentService esewaPaymentService;
    private final StudentFeeMapperService mapperService;
    private final MiscExpenseRepository miscRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public StudentFeeService(StudentFeeRepository feeRepository, StudentRepository studentRepository, FeeInstallmentRepository installmentRepository, EsewaPaymentService esewaPaymentService, StudentFeeMapperService mapperService, MiscExpenseRepository miscRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
        this.installmentRepository = installmentRepository;
        this.esewaPaymentService = esewaPaymentService;
        this.mapperService = mapperService;
        this.miscRepository = miscRepository;
    }

    @Transactional
    public StudentFee getStudentFeeByStudentId(Long studentId) {
        StudentFee fee = feeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student fee not found"));

        List<FeeInstallment> latest = installmentRepository.findByStudentFeeId(fee.getId());
        if (fee.getInstallments() == null) {
            fee.setInstallments(new ArrayList<>());
        } else {
            fee.getInstallments().clear();
        }
        fee.getInstallments().addAll(latest);

        for (FeeInstallment installment : latest) {
            installment.setStudentFee(fee);
        }

        recalculateInstallments(fee);
        return fee;
    }

    public void createInstallments(StudentFee studentFee) {
        int totalInstallments = studentFee.getTotalInstallments() > 0
                ? studentFee.getTotalInstallments()
                : Math.max(1, studentFee.getCourseDurationYears() * 2);
        BigDecimal installmentAmount = studentFee.getNetFee()
                .divide(BigDecimal.valueOf(totalInstallments), 2, RoundingMode.HALF_UP);
        List<FeeInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= totalInstallments; i++) {
            FeeInstallment installment = new FeeInstallment();
            installment.setInstallmentNumber(i);
            installment.setAmount(installmentAmount);
            installment.setPaid(false);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setPendingAmount(installmentAmount);
            installment.setStudentFee(studentFee);
            installment.setIsMiscExpense(false);
            installments.add(installment);
        }
        studentFee.setInstallments(installments);
    }

    public Optional<StudentFeeResponseDTO> assignFee(Long studentId, BigDecimal tuitionFee, BigDecimal examFee, BigDecimal ecaCharge, BigDecimal admissionFee, BigDecimal universityCharge, BigDecimal scholarship, BigDecimal discount, int years, int totalInstallments) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            System.out.println("Student not found with ID:" + studentId);
            return Optional.empty();
        }

        Student student = studentOpt.get();

        if (student.getStudentFee() != null) {
            System.out.println("Student already has fee assigned");
            return Optional.empty();
        }
        BigDecimal safeTuition = Objects.requireNonNullElse(tuitionFee, BigDecimal.ZERO);
        BigDecimal safeExam = Objects.requireNonNullElse(examFee, BigDecimal.ZERO);
        BigDecimal safeEca = Objects.requireNonNullElse(ecaCharge, BigDecimal.ZERO);
        BigDecimal safeAdmission = Objects.requireNonNullElse(admissionFee, BigDecimal.ZERO);
        BigDecimal safeUniversity = Objects.requireNonNullElse(universityCharge, BigDecimal.ZERO);
        BigDecimal safeScholar = Objects.requireNonNullElse(scholarship, BigDecimal.ZERO);
        BigDecimal safeDiscount = Objects.requireNonNullElse(discount, BigDecimal.ZERO);

        BigDecimal totalFee = safeTuition.add(safeExam).add(safeEca).add(safeAdmission).add(safeUniversity);
        BigDecimal netFee = totalFee.subtract(safeScholar).subtract(safeDiscount);

        StudentFee studentFee = new StudentFee();
        studentFee.setTuitionFee(safeTuition);
        studentFee.setExamFee(safeExam);
        studentFee.setEcaCharge(safeEca);
        studentFee.setAdmissionFee(safeAdmission);
        studentFee.setUniversityCharge(safeUniversity);
        studentFee.setTotalFee(totalFee);
        studentFee.setScholarshipAmount(safeScholar);
        studentFee.setDiscountAmount(safeDiscount);
        studentFee.setNetFee(netFee);
        studentFee.setCourseDurationYears(years);
        studentFee.setTotalInstallments(totalInstallments);
        studentFee.setStudent(student);

        createInstallments(studentFee);

        student.setStudentFee(studentFee);
        StudentFee saved = feeRepository.save(studentFee);
        studentRepository.save(student);

        return Optional.of(StudentFeeResponseDTO.fromEntity(saved));
    }

    public Optional<StudentFeeResponseDTO> getAssignedFee(Long studentId) {
        return studentRepository.findById(studentId)
                .map(Student::getStudentFee)
                .filter(Objects::nonNull)
                .map(mapperService::toDTO);
    }

    public Optional<StudentFeeResponseDTO> updateFee(Long studentId, BigDecimal admissionFee, BigDecimal tuitionFee, BigDecimal examFee, BigDecimal universityCharge, BigDecimal ecaCharge,
                                                     BigDecimal scholarship, BigDecimal discount,
                                                     Integer years, Integer totalInstallments) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return Optional.empty();
        }

        Student student = studentOpt.get();
        StudentFee fee = student.getStudentFee();
        if (fee == null) {
            return Optional.empty();
        }

        BigDecimal oldScholarship = fee.getScholarshipAmount();
        BigDecimal oldDiscount = fee.getDiscountAmount();
        BigDecimal oldNetFee = fee.getNetFee();

        if (admissionFee != null) fee.setAdmissionFee(admissionFee);
        if (tuitionFee != null) fee.setTuitionFee(tuitionFee);
        if (examFee != null) fee.setExamFee(examFee);
        if (universityCharge != null) fee.setUniversityCharge(universityCharge);
        if (ecaCharge != null) fee.setEcaCharge(ecaCharge);
        if (scholarship != null) fee.setScholarshipAmount(scholarship);
        if (discount != null) fee.setDiscountAmount(discount);
        if (years != null) fee.setCourseDurationYears(years);

        BigDecimal totalFee = safe(fee.getAdmissionFee())
                .add(safe(fee.getTuitionFee()))
                .add(safe(fee.getExamFee()))
                .add(safe(fee.getUniversityCharge()))
                .add(safe(fee.getEcaCharge()));

        BigDecimal newNetFee = totalFee
                .subtract(safe(fee.getScholarshipAmount()))
                .subtract(safe(fee.getDiscountAmount()));

        if (newNetFee.compareTo(BigDecimal.ZERO) < 0) {
            newNetFee = BigDecimal.ZERO;
        }

        fee.setTotalFee(totalFee);
        fee.setNetFee(newNetFee);

        boolean installmentsChanged = totalInstallments != null &&
                !Objects.equals(fee.getTotalInstallments(), totalInstallments);

        if (totalInstallments != null) {
            fee.setTotalInstallments(totalInstallments);
        }

        boolean financialAidChanged = !Objects.equals(oldScholarship, fee.getScholarshipAmount()) ||
                !Objects.equals(oldDiscount, fee.getDiscountAmount());

        if (financialAidChanged || installmentsChanged) {
            if (installmentsChanged) {
                recreateInstallmentsExcludingMisc(fee, totalInstallments);
            } else {
                adjustRegularInstallmentsForFinancialAidChange(fee, oldNetFee, newNetFee);
            }
        }
        StudentFee savedFee = feeRepository.save(fee);
        return Optional.of(mapperService.toDTO(savedFee));
    }

    private void recreateInstallmentsExcludingMisc(StudentFee fee, int totalInstallments) {
        try {
            List<FeeInstallment> miscInstallments = fee.getInstallments().stream()
                    .filter(this::isMiscInstallment)
                    .collect(Collectors.toList());

            List<FeeInstallment> regularInstallments = fee.getInstallments().stream()
                    .filter(inst -> !isMiscInstallment(inst))
                    .collect(Collectors.toList());

            List<FeeInstallment> paidRegularInstallments = regularInstallments.stream()
                    .filter(inst -> inst.isPaid() || inst.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            List<FeeInstallment> unpaidRegularInstallments = regularInstallments.stream()
                    .filter(inst -> !inst.isPaid() && inst.getPaidAmount().compareTo(BigDecimal.ZERO) == 0)
                    .collect(Collectors.toList());

            int paidCount = paidRegularInstallments.size();
            int miscCount = miscInstallments.size();

            if (totalInstallments < paidCount) {
                throw new IllegalArgumentException("Cannot reduce installments to " + totalInstallments +
                        ". Minimum required: " + (paidCount + 1) +
                        " (must have at least 1 unpaid installment after " + paidCount + " paid)");
            }

            fee.getInstallments().clear();

            for (int i = 0; i < paidRegularInstallments.size(); i++) {
                FeeInstallment paidInstallment = paidRegularInstallments.get(i);
                paidInstallment.setInstallmentNumber(i + 1);
                fee.getInstallments().add(paidInstallment);
            }

            BigDecimal totalPaidAmount = paidRegularInstallments.stream()
                    .map(FeeInstallment::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal remainingNetFee = fee.getNetFee().subtract(totalPaidAmount);

            int unpaidRegularToCreate = totalInstallments - paidCount;
            if (unpaidRegularToCreate > 0) {
                BigDecimal newInstallmentAmount = remainingNetFee.divide(
                        BigDecimal.valueOf(unpaidRegularToCreate), 2, RoundingMode.HALF_UP);

                int startNumber = paidCount + 1;
                for (int i = 0; i < unpaidRegularToCreate; i++) {
                    FeeInstallment installment = new FeeInstallment();
                    installment.setInstallmentNumber(startNumber + i);
                    installment.setAmount(newInstallmentAmount);
                    installment.setStudentFee(fee);
                    installment.setPaid(false);
                    installment.setPaidAmount(BigDecimal.ZERO);
                    installment.setPendingAmount(newInstallmentAmount);
                    installment.setIsMiscExpense(false);

                    fee.getInstallments().add(installment);
                }
            }

            int nextNumber = fee.getInstallments().size() + 1;
            for (FeeInstallment miscInst : miscInstallments) {
                miscInst.setInstallmentNumber(nextNumber++);
                fee.getInstallments().add(miscInst);
            }

            fee.setTotalInstallments(fee.getInstallments().size());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to recreate installments: " + e.getMessage(), e);
        }
    }

    private void adjustRegularInstallmentsForFinancialAidChange(StudentFee fee, BigDecimal oldNetFee, BigDecimal newNetFee) {
        if (fee.getInstallments() == null || fee.getInstallments().isEmpty()) {
            return;
        }

        List<FeeInstallment> regularInstallments = fee.getInstallments().stream()
                .filter(inst -> !isMiscInstallment(inst))
                .collect(Collectors.toList());

        if (regularInstallments.isEmpty()) {
            return;
        }

        BigDecimal netFeeDifference = newNetFee.subtract(oldNetFee);

        if (netFeeDifference.compareTo(BigDecimal.ZERO) > 0) {
            distributeIncreaseToUnpaidRegularInstallments(regularInstallments, netFeeDifference);
        }
        else if (netFeeDifference.compareTo(BigDecimal.ZERO) < 0) {
            distributeDecreaseToUnpaidRegularInstallments(regularInstallments, netFeeDifference.abs());
        }
    }

    private void distributeIncreaseToUnpaidRegularInstallments(List<FeeInstallment> regularInstallments, BigDecimal increaseAmount) {
        List<FeeInstallment> unpaidInstallments = regularInstallments.stream()
                .filter(inst -> !inst.isPaid() && inst.getPendingAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(FeeInstallment::getInstallmentNumber))
                .collect(Collectors.toList());

        if (unpaidInstallments.isEmpty()) {
            return;
        }

        BigDecimal remainingIncrease = increaseAmount;
        for (FeeInstallment installment : unpaidInstallments) {
            if (remainingIncrease.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal newAmount = installment.getAmount().add(remainingIncrease);
            installment.setAmount(newAmount);
            installment.setPendingAmount(installment.getPendingAmount().add(remainingIncrease));
            remainingIncrease = BigDecimal.ZERO;
        }
    }

    private void distributeDecreaseToUnpaidRegularInstallments(List<FeeInstallment> regularInstallments, BigDecimal decreaseAmount) {
        List<FeeInstallment> unpaidInstallments = regularInstallments.stream()
                .filter(inst -> !inst.isPaid() && inst.getPendingAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(FeeInstallment::getInstallmentNumber))
                .collect(Collectors.toList());

        if (unpaidInstallments.isEmpty()) {
            return;
        }

        BigDecimal remainingDecrease = decreaseAmount;
        for (FeeInstallment installment : unpaidInstallments) {
            if (remainingDecrease.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal maxReduction = installment.getPendingAmount();
            BigDecimal actualReduction = remainingDecrease.min(maxReduction);

            BigDecimal newAmount = installment.getAmount().subtract(actualReduction);
            BigDecimal newPending = installment.getPendingAmount().subtract(actualReduction);

            installment.setAmount(newAmount);
            installment.setPendingAmount(newPending);

            if(newPending.compareTo(BigDecimal.ZERO) == 0){
                installment.setPaid(true);
                installment.setPaidAmount(installment.getAmount());
            }
            remainingDecrease = remainingDecrease.subtract(actualReduction);
        }
    }

    @Transactional
    public void recalculateTotalFee(StudentFee fee) {
        BigDecimal components = safe(fee.getAdmissionFee())
                .add(safe(fee.getTuitionFee()))
                .add(safe(fee.getExamFee()))
                .add(safe(fee.getUniversityCharge()))
                .add(safe(fee.getEcaCharge()));

        BigDecimal discount = safe(fee.getDiscountAmount());
        BigDecimal scholarship = safe(fee.getScholarshipAmount());

        BigDecimal totalFee = components;
        BigDecimal netFee = totalFee.subtract(discount).subtract(scholarship);

        if (netFee.compareTo(BigDecimal.ZERO) < 0) netFee = BigDecimal.ZERO;
        fee.setTotalFee(totalFee);
        fee.setNetFee(netFee);
        feeRepository.save(fee);
    }

    @Transactional
    public void addMiscExpense(Long studentId, BigDecimal amount, String description) {
        StudentFee fee = feeRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student fee not found"));

        if (fee.getMiscExpenses() == null) {
            fee.setMiscExpenses(new ArrayList<>());
        }

        MiscExpense expense = new MiscExpense();
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setStudentFee(fee);
        miscRepository.save(expense);

        fee.getMiscExpenses().add(expense);

        int newInstallmentNumber = getNextInstallmentNumber(fee);

        FeeInstallment miscInstallment = new FeeInstallment();
        miscInstallment.setInstallmentNumber(newInstallmentNumber);
        miscInstallment.setAmount(amount);
        miscInstallment.setPaid(false);
        miscInstallment.setPaidAmount(BigDecimal.ZERO);
        miscInstallment.setPendingAmount(amount);
        miscInstallment.setDescription("MISC: " + description);
        miscInstallment.setStudentFee(fee);
        miscInstallment.setIsMiscExpense(true);

        installmentRepository.save(miscInstallment);

        if (fee.getInstallments() == null) {
            fee.setInstallments(new ArrayList<>());
        }
        fee.getInstallments().add(miscInstallment);

        fee.setTotalInstallments(fee.getInstallments().size());

        feeRepository.save(fee);
    }

    private int getNextInstallmentNumber(StudentFee fee) {
        if (fee.getInstallments() == null || fee.getInstallments().isEmpty()) {
            return 1;
        }
        return fee.getInstallments().stream()
                .mapToInt(FeeInstallment::getInstallmentNumber)
                .max()
                .orElse(0) + 1;
    }

    private void recalculateInstallments(StudentFee fee) {
        if (fee.getInstallments() == null || fee.getInstallments().isEmpty()) {
            System.out.println("No installments to recalculate");
            return;
        }

        List<FeeInstallment> miscInstallments = fee.getInstallments().stream()
                .filter(this::isMiscInstallment)
                .collect(Collectors.toList());

        List<FeeInstallment> regularInstallments = fee.getInstallments().stream()
                .filter(inst -> !isMiscInstallment(inst))
                .collect(Collectors.toList());

        List<FeeInstallment> paidRegularInstallments = regularInstallments.stream()
                .filter(inst -> inst.isPaid() || inst.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        List<FeeInstallment> unpaidRegularInstallments = regularInstallments.stream()
                .filter(inst -> !inst.isPaid() && inst.getPaidAmount().compareTo(BigDecimal.ZERO) == 0)
                .collect(Collectors.toList());

        if (unpaidRegularInstallments.isEmpty()) {
            return;
        }

        BigDecimal totalPaidAmount = paidRegularInstallments.stream()
                .map(FeeInstallment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingNetFee = fee.getNetFee().subtract(totalPaidAmount);
        BigDecimal newInstallmentAmount = remainingNetFee.divide(
                BigDecimal.valueOf(unpaidRegularInstallments.size()), 2, RoundingMode.HALF_UP);

        for (FeeInstallment installment : unpaidRegularInstallments) {
            BigDecimal oldAmount = installment.getAmount();
            installment.setAmount(newInstallmentAmount);
            installment.setPendingAmount(newInstallmentAmount.subtract(installment.getPaidAmount()));
        }
        feeRepository.save(fee);
    }

    @Transactional
    public Optional<StudentFeeResponseDTO> patchFee(
            Long studentId,
            BigDecimal admissionFee,
            BigDecimal tuitionFee,
            BigDecimal examFee,
            BigDecimal universityCharge,
            BigDecimal ecaCharge,
            BigDecimal scholarship,
            BigDecimal discount,
            Integer years,
            Integer totalInstallments
    ) {
        Optional<StudentFee> feeOpt = feeRepository.findByStudentId(studentId);
        if (feeOpt.isEmpty()) {
            return Optional.empty();
        }

        StudentFee studentFee = feeOpt.get();

        BigDecimal oldScholarship = studentFee.getScholarshipAmount();
        BigDecimal oldDiscount = studentFee.getDiscountAmount();
        BigDecimal oldNetFee = studentFee.getNetFee();

        if (admissionFee != null) studentFee.setAdmissionFee(admissionFee);
        if (tuitionFee != null) studentFee.setTuitionFee(tuitionFee);
        if (examFee != null) studentFee.setExamFee(examFee);
        if (universityCharge != null) studentFee.setUniversityCharge(universityCharge);
        if (ecaCharge != null) studentFee.setEcaCharge(ecaCharge);
        if (scholarship != null) studentFee.setScholarshipAmount(scholarship);
        if (discount != null) studentFee.setDiscountAmount(discount);
        if (years != null) studentFee.setCourseDurationYears(years);

        boolean installmentsChanged = totalInstallments != null &&
                !Objects.equals(studentFee.getTotalInstallments(), totalInstallments);

        if (totalInstallments != null) {
            studentFee.setTotalInstallments(totalInstallments);
        }

        recalculateTotalFee(studentFee);
        BigDecimal newNetFee = studentFee.getNetFee();

        boolean financialAidChanged = !Objects.equals(oldScholarship, studentFee.getScholarshipAmount()) ||
                !Objects.equals(oldDiscount, studentFee.getDiscountAmount());

        if (financialAidChanged || installmentsChanged) {
            if (installmentsChanged) {
                recreateInstallmentsExcludingMisc(studentFee, totalInstallments);
            } else {
                adjustRegularInstallmentsForFinancialAidChange(studentFee, oldNetFee, newNetFee);
            }
        }

        StudentFee saved = feeRepository.save(studentFee);
        return Optional.of(mapperService.toDTO(saved));
    }

    private boolean isMiscInstallment(FeeInstallment installment) {
        return installment.getDescription() != null &&
                installment.getDescription().toLowerCase().contains("misc");
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    public boolean deleteFee(Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) return false;
        Student student = studentOpt.get();
        StudentFee fee = student.getStudentFee();
        if (fee == null) return false;

        student.setStudentFee(null);
        studentRepository.save(student);
        feeRepository.delete(fee);
        return true;
    }

    @Transactional
    public Optional<FeeInstallment> payInstallmentByNumber(Long studentId, int installmentNumber) {
        Optional<FeeInstallment> installmentOpt = installmentRepository.findByStudentFee_Student_IdAndInstallmentNumber(studentId, installmentNumber);
        if (installmentOpt.isEmpty()) return Optional.empty();
        FeeInstallment feeInstallment = installmentOpt.get();

        if(feeInstallment.isPaid()){
            throw new RuntimeException("Installment already paid");
        }
        String transactionId = "TXN-" + feeInstallment.getId();
        String refId = "REF-" + feeInstallment.getId();
        String productId = "COLLEGE_FEE_" + feeInstallment.getId();

        boolean paymentVerified = esewaPaymentService.verifyPayment(transactionId, feeInstallment.getAmount().doubleValue(), refId, productId);
        if (!paymentVerified) {
            throw new RuntimeException("Payment verification failed for installment ID:" + installmentNumber);
        }

        feeInstallment.updatePayment(feeInstallment.getAmount());

        FeeInstallment saved = installmentRepository.save(feeInstallment);
        return Optional.of(saved);
    }

    @Transactional
    public Optional<FeeInstallment> makePartialPayment(Long installmentId, BigDecimal paymentAmount) {
        Optional<FeeInstallment> installmentOpt = installmentRepository.findById(installmentId);
        if (installmentOpt.isEmpty() || paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        FeeInstallment installment = installmentOpt.get();
        BigDecimal currentPaidAmount = installment.getPaidAmount();
        BigDecimal newPaidAmount = currentPaidAmount.add(paymentAmount);
        BigDecimal installmentAmount = installment.getAmount();

        if (newPaidAmount.compareTo(installmentAmount) > 0) {
            throw new RuntimeException("Payment amount exceeds the installment amount");
        }

        installment.updatePayment(paymentAmount);

        FeeInstallment saved = installmentRepository.save(installment);

        recalculateInstallments(saved.getStudentFee());

        return Optional.of(saved);
    }

    public Optional<FeeInstallment> getInstallmentByNumber(Long studentId, int installmentNumber) {
        return installmentRepository.findByStudentFee_Student_IdAndInstallmentNumber(studentId, installmentNumber);
    }

    public BigDecimal getRemainingAmount(Long studentId) {
        List<FeeInstallment> installments = installmentRepository.findByStudentFee_Student_Id(studentId);
        return installments.stream()
                .map(FeeInstallment::getPendingAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public double getInstallmentAmountByNumber(Long studentId, int installmentNumber) {
        FeeInstallment installment = installmentRepository.findByStudentFee_Student_IdAndInstallmentNumber(studentId, installmentNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Installment number " + installmentNumber + " not found for student " + studentId
                ));
        return installment.getAmount().doubleValue();
    }
    public boolean isInstallmentPaid(Long studentId, int installmentNumber) {
        FeeInstallment installment = installmentRepository
                .findByStudentFee_Student_IdAndInstallmentNumber(studentId, installmentNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Installment number " + installmentNumber + " not found for student " + studentId
                ));
        return installment.isPaid();
    }

    @Transactional
    public Optional<FeeInstallment> payInstallment(Long installmentId) {
        Optional<FeeInstallment> installmentOpt = installmentRepository.findById(installmentId);
        if (installmentOpt.isEmpty()) return Optional.empty();
        FeeInstallment feeInstallment = installmentOpt.get();

        if (feeInstallment.isPaid()) {
            throw new RuntimeException("Installment already paid");
        }

        String transactionId = "TXN-" + installmentId;
        String refId = "REF-" + installmentId;
        String productId = "COLLEGE_FEE_" + installmentId;

        boolean paymentVerified = esewaPaymentService.verifyPayment(transactionId, feeInstallment.getAmount().doubleValue(), refId, productId);
        if (!paymentVerified) {
            throw new RuntimeException("Payment verification failed for installment ID:" + installmentId);
        }

        feeInstallment.updatePayment(feeInstallment.getAmount());
        FeeInstallment saved = installmentRepository.save(feeInstallment);
        return Optional.of(saved);
    }
}