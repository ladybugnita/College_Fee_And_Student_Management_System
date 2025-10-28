package com.example.collegefeeandstudentmanagement.dto;
import java.math.BigDecimal;
public class AssignFeeRequest {
    private BigDecimal tuitionFee;
    private BigDecimal examFee;
    private BigDecimal ecaCharge;
    private BigDecimal admissionFee;
    private BigDecimal universityCharge;
    private BigDecimal totalFee;
    private BigDecimal scholarship;
    private BigDecimal discount;
    private Integer years;
    private Integer totalInstallments;

    public AssignFeeRequest(){}

    public AssignFeeRequest(BigDecimal tuitionFee, BigDecimal admissionFee, BigDecimal universityCharge, BigDecimal ecaCharge, BigDecimal examFee,BigDecimal totalFee, BigDecimal scholarship, BigDecimal discount, Integer years, Integer totalInstallments){
       this.tuitionFee = tuitionFee;
       this.admissionFee = admissionFee;
       this.universityCharge = universityCharge;
       this.ecaCharge = ecaCharge;
       this.examFee = examFee;
        this.totalFee = totalFee;
        this.scholarship= scholarship;
        this.discount = discount;
        this.years = years;
        this.totalInstallments = totalInstallments;
    }
    public BigDecimal getTuitionFee(){
        return tuitionFee;
    }
    public void setTuitionFee(BigDecimal tuitionFee){
        this.tuitionFee = tuitionFee;
    }
    public BigDecimal getAdmissionFee(){
        return admissionFee;
    }
    public void setAdmissionFee(BigDecimal admissionFee){
        this.admissionFee = admissionFee;
    }
    public BigDecimal getUniversityCharge(){
        return universityCharge;
    }
    public void setUniversityCharge(BigDecimal universityCharge){
        this.universityCharge= universityCharge;
    }
    public BigDecimal getEcaCharge(){
        return ecaCharge;
    }
    public void setEcaCharge(BigDecimal ecaCharge){
        this.ecaCharge = ecaCharge;
    }
    public BigDecimal getExamFee(){
        return examFee;
    }
    public void setExamFee(BigDecimal examFee){
        this.examFee = examFee;
    }
    public BigDecimal getTotalFee(){
        return totalFee;
    }
    public void setTotalFee(BigDecimal totalFee){
        this.totalFee = totalFee;
    }
    public BigDecimal getScholarship(){
        return scholarship;
    }
    public void setScholarship(BigDecimal scholarship){
        this.scholarship= scholarship;
    }
    public BigDecimal getDiscount(){
        return discount;
    }
    public void setDiscount(BigDecimal discount){
        this.discount = discount;
    }
    public Integer getYears(){
        return years;
    }
    public void setYears(Integer years){
        this.years = years;
    }
    public Integer getTotalInstallments(){
        return totalInstallments;
    }
    public void setTotalInstallments(Integer totalInstallments){
       this.totalInstallments = totalInstallments;
    }

    @Override
    public String toString(){
        return "AssignFeeRequest{" +
                ",tuitionFee =" + tuitionFee + ", admissionFee =" + admissionFee + ",ecaCharge ="+ ecaCharge + ",universityCharge= "+ universityCharge + ",examFee="+ examFee +
                ",scholarship="+ scholarship +
                ",discount=" + discount +
                ",years="+ years +
                ", totalInstallments="+ totalInstallments +
                '}';
    }
}
