package com.example.collegefeeandstudentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name ="student_fees", uniqueConstraints = {
        @UniqueConstraint(columnNames = "student_id")
})
public class StudentFee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name= "student_id",nullable =false, unique =true)
    @JsonBackReference
    private Student student;

    @Column(nullable = false)
    private BigDecimal totalFee;

    private BigDecimal scholarshipAmount= BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal admissionFee;
    private BigDecimal tuitionFee;
    private BigDecimal examFee;
    private BigDecimal universityCharge;
    private BigDecimal ecaCharge;

    @Column(nullable =false)
    private BigDecimal netFee;

    private Integer courseDurationYears;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name= "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "studentFee",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<FeeInstallment> installments = new ArrayList<>();

    @OneToMany(mappedBy = "studentFee", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<MiscExpense> miscExpenses;

    public StudentFee(){}
        public StudentFee(Student student, BigDecimal totalFee, BigDecimal scholarshipAmount, BigDecimal discountAmount, BigDecimal netFee, Integer years, BigDecimal admissionFee, BigDecimal tuitionFee, BigDecimal examFee, BigDecimal universityCharge, BigDecimal ecaCharge, Integer totalInstallments)
        {
            this.student = student;
            this.totalFee = totalFee;
            this.scholarshipAmount = scholarshipAmount;
            this.discountAmount = discountAmount;
            this.netFee = totalFee.subtract(scholarshipAmount).subtract(discountAmount);
            this.courseDurationYears = years;
            this.admissionFee = admissionFee;
            this.tuitionFee = tuitionFee;
            this.examFee = examFee;
            this.universityCharge = universityCharge;
            this.ecaCharge = ecaCharge;
            this.totalInstallments = totalInstallments;
        }

        public void calculateFees(){
         BigDecimal total = BigDecimal.ZERO;
         if(admissionFee != null) total = total.add(admissionFee);
         if(tuitionFee != null) total = total.add(tuitionFee);
         if(examFee != null) total = total.add(examFee);
         if(universityCharge != null) total = total.add(universityCharge);
         if(ecaCharge != null) total = total.add(ecaCharge);

         this.totalFee = total;

         BigDecimal net = total;
         if(discountAmount != null) net = net.subtract(discountAmount);
         if(scholarshipAmount != null) net = net.subtract(scholarshipAmount);

         this.netFee = net.max(BigDecimal.ZERO);
        }

        public Long getId(){
            return id;
        }
        public Student getStudent(){
                return student;
            }
            public void setStudent(Student student){
            this.student =student;
        }
        public BigDecimal getTotalFee(){
            return totalFee;
        }
        public void setTotalFee(BigDecimal totalFee){
            this.totalFee =totalFee;
        }
        public BigDecimal getScholarshipAmount(){
            return scholarshipAmount;
        }
        public void setScholarshipAmount(BigDecimal scholarshipAmount){
            this.scholarshipAmount = scholarshipAmount;
        }
        public BigDecimal getDiscountAmount(){
            return discountAmount;
        }
        public void setDiscountAmount(BigDecimal discountAmount){
            this.discountAmount = discountAmount;
        }
        public BigDecimal getNetFee(){
            return netFee;
        }
        public void setNetFee(BigDecimal netFee){
            this.netFee= netFee;
        }
        public Integer getCourseDurationYears(){
            return courseDurationYears;
        }
        public void setCourseDurationYears(Integer courseDurationYears){
            this.courseDurationYears =  courseDurationYears;
        }
        public List<FeeInstallment> getInstallments() {
            return installments;
        }
        public void setInstallments(List<FeeInstallment> installments){
            this.installments = installments;
        }
        public Integer getTotalInstallments(){
        return totalInstallments;
        }
        public void setTotalInstallments(Integer totalInstallments){
        this.totalInstallments = totalInstallments;
        }
        public BigDecimal getAdmissionFee(){
        return admissionFee;
        }
        public void setAdmissionFee(BigDecimal admissionFee){
        this.admissionFee = admissionFee;
        }
        public BigDecimal getTuitionFee(){
        return tuitionFee;
        }
        public void setTuitionFee(BigDecimal tuitionFee){
        this.tuitionFee = tuitionFee;
        }
        public BigDecimal getExamFee(){
        return examFee;
        }
        public void setExamFee(BigDecimal examFee){
        this.examFee = examFee;
        }
        public BigDecimal getUniversityCharge(){
        return universityCharge;
        }
        public void setUniversityCharge(BigDecimal universityCharge){
        this.universityCharge = universityCharge;
        }
        public BigDecimal getEcaCharge(){
        return ecaCharge;
        }
        public void setEcaCharge(BigDecimal ecaCharge){
        this.ecaCharge = ecaCharge;
        }
        public List<MiscExpense> getMiscExpenses(){
        return miscExpenses;
        }
        public void setMiscExpenses(List<MiscExpense> miscExpenses){
        this.miscExpenses = miscExpenses;
        }
}

