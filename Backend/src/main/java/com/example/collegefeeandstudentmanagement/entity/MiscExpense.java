package com.example.collegefeeandstudentmanagement.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class MiscExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private BigDecimal amount;
    private Instant createdAt = Instant.now();

    @ManyToOne
    @JoinColumn(name = "student_fee_id")
    @JsonBackReference
    private StudentFee studentFee;

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
    public StudentFee getStudentFee(){
        return studentFee;
    }
    public void setStudentFee(StudentFee studentFee){
        this.studentFee = studentFee;
    }
}
