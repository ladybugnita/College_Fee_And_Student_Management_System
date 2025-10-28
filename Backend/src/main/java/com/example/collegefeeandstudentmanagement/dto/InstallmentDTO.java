package com.example.collegefeeandstudentmanagement.dto;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstallmentDTO {
    private Long id;
    private int installmentNumber;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal pendingAmount;
    private boolean paid;
    private String description;

    public InstallmentDTO(){}

    public InstallmentDTO(Long id, int installmentNumber,BigDecimal amount, boolean paid, BigDecimal paidAmount, BigDecimal pendingAmount, String description){
        this.id = id;
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.paid = paid;
        this.paidAmount = paidAmount;
        this.pendingAmount = pendingAmount;
        this.description = description;
    }

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public int getInstallmentNumber(){
        return installmentNumber;
    }
    public void setInstallmentNumber(int installmentNumber){
        this.installmentNumber = installmentNumber;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public void setAmount(BigDecimal amount){
        this.amount = amount;
    }
    public BigDecimal getPaidAmount(){
        return paidAmount;
    }
    public void setPaidAmount(BigDecimal paidAmount){
        this.paidAmount = paidAmount;
    }
    public BigDecimal getPendingAmount(){
        return pendingAmount;
    }
    public void setPendingAmount(BigDecimal pendingAmount){
        this.pendingAmount = pendingAmount;
    }
    public boolean getPaid(){
        return paid;
    }
    public void setPaid(boolean paid){
        this.paid = paid;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
}
