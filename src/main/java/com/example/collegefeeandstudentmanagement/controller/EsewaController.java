package com.example.collegefeeandstudentmanagement.controller;

import com.example.collegefeeandstudentmanagement.dto.EsewaResponseDTO;
import com.example.collegefeeandstudentmanagement.entity.FeeInstallment;
import com.example.collegefeeandstudentmanagement.entity.Student;
import com.example.collegefeeandstudentmanagement.repository.FeeInstallmentRepository;
import com.example.collegefeeandstudentmanagement.service.EsewaPaymentService;
import com.example.collegefeeandstudentmanagement.service.StudentFeeMapperService;
import com.example.collegefeeandstudentmanagement.service.StudentFeeService;
import com.example.collegefeeandstudentmanagement.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/esewa")
public class EsewaController {

    private final EsewaPaymentService esewaPaymentService;
    private final StudentFeeService studentFeeService;
    private final StudentRepository studentRepository;
    private final FeeInstallmentRepository installmentRepository;
    private final StudentFeeMapperService studentFeeMapperService;

    public EsewaController(EsewaPaymentService esewaPaymentService,
                           StudentFeeService studentFeeService,
                           StudentRepository studentRepository,
                           FeeInstallmentRepository installmentRepository,
                           StudentFeeMapperService studentFeeMapperService) {
        this.esewaPaymentService = esewaPaymentService;
        this.studentFeeService = studentFeeService;
        this.studentRepository = studentRepository;
        this.installmentRepository = installmentRepository;
        this.studentFeeMapperService = studentFeeMapperService;
    }
    @PostMapping("/installment/pay")
    public ResponseEntity<?> payInstallment(@RequestParam Long studentId,
                                            @RequestParam int installmentNumber,
                                            @RequestParam BigDecimal payAmount,
                                            Authentication auth) {

        Student student = studentRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getId().equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You cannot pay for another student's installment");
        }

        FeeInstallment installment = studentFeeService.getInstallmentByNumber(studentId, installmentNumber)
                .orElseThrow(() -> new RuntimeException("Installment not found"));

        if (installment.isPaid()) {
            return ResponseEntity.badRequest()
                    .body("This installment is already fully paid");
        }

        BigDecimal remainingForThisInstallment = installment.getAmount()
                .subtract(installment.getPaidAmount() != null ? installment.getPaidAmount() : BigDecimal.ZERO);

        if (payAmount.compareTo(remainingForThisInstallment) > 0) {
            return ResponseEntity.badRequest().body(
                    "Overpayment detected!" + "you tried to pay:" + payAmount + "but remaining amount for installment" + installmentNumber + "is only:" + remainingForThisInstallment + ". Please pay exactly" + remainingForThisInstallment + "to complete this installment.");
        }

        if (installment.getStudentFee().getTotalInstallments() == null) {
            return ResponseEntity.badRequest()
                    .body("Total installments not set for this student. Update fee details first.");
        }

        int totalInstallments = installment.getStudentFee().getTotalInstallments();
        if (installmentNumber == totalInstallments) {
            BigDecimal remaining = studentFeeService.getRemainingAmount(studentId);
            if (payAmount.compareTo(remaining) < 0) {
                return ResponseEntity.badRequest()
                        .body("You must pay all remaining installments in the last installment!");
            }
        }

        String transactionId = "TXN-" + studentId + "-" + installmentNumber + "-" + System.currentTimeMillis();

        Map<String, String> payload = new HashMap<>();
        payload.put("tAmt", payAmount.toString());
        payload.put("amt", payAmount.toString());
        payload.put("txAmt", "0");
        payload.put("psc", "0");
        payload.put("pdc", "0");
        payload.put("scd", "EPAYTEST");
        payload.put("pid", transactionId);
        payload.put("su", "http://localhost:8080/api/esewa/success?studentId=" + studentId + "&installmentNumber=" + installmentNumber);
        payload.put("fu", "http://localhost:8080/api/esewa/failure");

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/success")
    public ResponseEntity<?> successCallback(@RequestParam("oid") String transactionId,
                                                            @RequestParam("amt") double amount,
                                                            @RequestParam("refId") String refId,
                                                            @RequestParam("productId") String productId,
                                                            @RequestParam(value = "studentId", required = false) Long studentId,
                                                            @RequestParam(value = "installmentNumber", required = false) Integer installmentNumber) {
        System.out.println("esewa Success callback Received!");
        System.out.println("Transaction ID:" + transactionId);
        System.out.println("Amount:" + amount);
        System.out.println("Reference ID:" + refId);
        System.out.println("Product ID:" + productId);
        System.out.println("Student ID:" + studentId);
        System.out.println("Installment Number:" + installmentNumber);

        EsewaResponseDTO response = new EsewaResponseDTO();
        response.setTransactionId(transactionId);
        response.setRefId(refId);
        response.setAmount(amount);
        response.setStudentId(studentId);

        boolean verified = esewaPaymentService.verifyPayment(transactionId, amount, refId, productId);

        if (verified) {
            if (productId.startsWith("TXN-ALL-")) {
                response.setMessage("All installments paid successfully!");
                return handlePayAll(transactionId, productId);

            } else {
                 response.setMessage("Payment successful and installment marked as paid!");
                 return handleSingleInstallment(studentId, installmentNumber, transactionId, amount);
            }
        } else {
            response.setMessage("payment verification failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private ResponseEntity<?> handlePayAll(String transactionId, String productId) {
        try {
            Long studentId = extractStudentIdFromProductId(productId);
            System.out.println("processing pay all for student: " + studentId);
            List<FeeInstallment> allInstallments = installmentRepository.findByStudentFee_Student_Id(studentId);
            List<FeeInstallment> unpaidInstallments = allInstallments.stream()
                    .filter(installment -> !installment.isPaid())
                    .collect(Collectors.toList());
            int paidCount = 0;

            for (FeeInstallment installment : unpaidInstallments) {
                installment.setPaid(true);
                installment.setPaidAmount(installment.getAmount());
                installment.setPendingAmount(BigDecimal.ZERO);
                installmentRepository.save(installment);
                paidCount++;
                System.out.println("Paid installment" + installment.getInstallmentNumber());
            }

            var updatedFee = studentFeeService.getStudentFeeByStudentId(studentId);
            var feeDTO = studentFeeMapperService.toDTO(updatedFee);
            return ResponseEntity.ok(Map.of(
                    "message", "All installments paid successfully!",
                    "paidCount", paidCount,
                    "studentId", studentId,
                    "studentFee", feeDTO
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing pay all:" + e.getMessage());
        }
    }

    private ResponseEntity<?> handleSingleInstallment(Long studentId, Integer installmentNumber, String transactionId, double amount) {
        try {
            if (studentId == null || installmentNumber == null) {
                return ResponseEntity.badRequest().body("Missing studentId or installmentNumber for single payment");
            }
            FeeInstallment installment = studentFeeService.getInstallmentByNumber(studentId, installmentNumber)
                    .orElseThrow(() -> new RuntimeException("Installment not found"));

            BigDecimal payAmount = BigDecimal.valueOf(amount);
            BigDecimal alreadyPaid = installment.getPaidAmount() != null ? installment.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal totalPaid = alreadyPaid.add(payAmount);
            BigDecimal totalAmount = installment.getAmount();

            if (totalPaid.compareTo(totalAmount) > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Overpayment detected",
                        "message", "You tried to pay" + payAmount + ", but only " + totalAmount.subtract(alreadyPaid) + "was pending."
                ));
            }
            installment.setPaidAmount(totalPaid);
            installment.setPendingAmount(totalAmount.subtract(totalPaid));

            if (installment.getPendingAmount().compareTo(BigDecimal.ZERO) == 0){
                installment.setPaid(true);
        }
        else{
            installment.setPaid(false);
        }
        installmentRepository.save(installment);

        var updatedFee = studentFeeService.getAssignedFee(studentId)
                .orElseThrow(() -> new RuntimeException("Student fee not found after payment."));

            return ResponseEntity.ok(Map.of(
                    "transactionId", transactionId,
                    "installmentNumber", installmentNumber,
                    "studentId", studentId,
                    "studentFee", updatedFee,
                    "message", "Payment of Rs." + amount + "received successfully!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Payment failed",
                    "message", e.getMessage()
            ));
        }
    }

    private Long extractStudentIdFromProductId(String productId) {
        String[] parts = productId.split("-");
        if (parts.length >= 3) {
            return Long.parseLong(parts[2]);
        }
        throw new RuntimeException("cannot extract student ID from:" + productId);
    }

    @GetMapping("/failure")
    public ResponseEntity<?> failureCallback(@RequestParam(value = "oid", required = false) String transactionId,
                                                            @RequestParam (value ="amt", required = false) Double amount,
                                                            @RequestParam(value = "refId", required = false) String refId){
        EsewaResponseDTO response = new EsewaResponseDTO();
        response.setTransactionId(transactionId);
        response.setRefId(refId);
        response.setAmount(amount);
        response.setMessage("Payment failed or cancelled.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @PostMapping("/pay-all/{studentId}")
    public ResponseEntity<?> payAllInstallments(@PathVariable Long studentId,
                                                Authentication auth){

        Student student = studentRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if(!student.getId().equals(studentId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You cannot pay for another student's installments");
        }

        BigDecimal remaining = studentFeeService.getRemainingAmount(studentId);
        if(remaining.compareTo(BigDecimal.ZERO) <= 0){
            return ResponseEntity.badRequest().body("All installments already paid.");
        }

        String transactionId = "TXN-ALL-" + studentId + "-" + System.currentTimeMillis();
        Map<String, String> payload = esewaPaymentService.initiatePayment(transactionId, remaining.doubleValue());

        return ResponseEntity.ok(payload);
    }
}
