package com.example.collegefeeandstudentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table (name = "fee_installments")
public class FeeInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;


    @Column(nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Column(name = "paid", nullable = false)
    private boolean paid = false;

    private String description;

    @Column(name = "is_misc_expense")
    private boolean isMiscExpense = false;

    @ManyToOne
    @JoinColumn(name = "student_fee_id")
    @JsonBackReference
    private StudentFee studentFee;

    public FeeInstallment() {
    }

    public FeeInstallment(int installmentNumber, BigDecimal amount, StudentFee studentFee, String description) {
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.pendingAmount = amount;
        this.studentFee = studentFee;
        this.paid = false;
        this.description = description;
        this.isMiscExpense = false;
    }

    public void updatePayment(BigDecimal payAmount) {
        if (payAmount == null) {
            throw new IllegalArgumentException("payment amount cannot be null");
        }
        if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }
        if (this.pendingAmount == null) {
            this.pendingAmount = this.amount;
        }
        BigDecimal maxAllowedPayment = this.amount.subtract(this.paidAmount);
        System.out.println("Payment attempt:" + payAmount + "| Max allowed:" + maxAllowedPayment + "| Already paid:" + this.paidAmount);

        if (payAmount.compareTo(maxAllowedPayment) > 0) {
            throw new IllegalArgumentException("Overpayment detected!" +
                    "you tried to pay:" + payAmount + "but maximum allowed is:" + maxAllowedPayment + "(Installment amount:" + this.amount + ", Already paid:" + this.paidAmount + ")");
        }
        this.paidAmount = this.paidAmount.add(payAmount);
        this.pendingAmount = this.amount.subtract(this.paidAmount);

        if (this.paidAmount.compareTo(this.amount) >= 0) {
            this.paid = true;
            this.pendingAmount = BigDecimal.ZERO;
            if (this.paidAmount.compareTo(this.amount) > 0) {
                this.paidAmount = this.amount;
            }
        }
    }

    public Long getId() {
        return id;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public StudentFee getStudentFee() {
        return studentFee;
    }

    public void setStudentFee(StudentFee studentFee) {
        this.studentFee = studentFee;
    }

    public boolean isPaid() {
        return paid;
    }

    public boolean getPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsMiscExpense() {
        return isMiscExpense;
    }

    public void setIsMiscExpense(Boolean isMiscExpense) {
        this.isMiscExpense = isMiscExpense != null? isMiscExpense : false;
    }
}
