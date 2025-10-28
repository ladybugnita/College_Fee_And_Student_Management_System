package com.example.collegefeeandstudentmanagement.dto;
import java.math.BigDecimal;
import  java.time.Instant;

public class MiscExpenseDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Instant createdAt;

    public MiscExpenseDTO(){}
    public MiscExpenseDTO(Long id, BigDecimal amount, String description, Instant createdAt){
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.createdAt = createdAt;
    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public void setAmount(BigDecimal amount){
        this.amount = amount;
    }
    public Instant getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt){
        this.createdAt = createdAt;
    }
}
