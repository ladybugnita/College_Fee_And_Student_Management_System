package com.example.collegefeeandstudentmanagement.dto;

public class EsewaResponseDTO {
    private String transactionId;
    private String refId;
    private double amount;
    private Long studentId;
    private String message;

    public String getTransactionId(){
        return transactionId;
    }
    public void setTransactionId(String transactionId){
        this.transactionId = transactionId;
    }
    public String getRefId(){
        return refId;
    }
    public void setRefId(String refId){
        this.refId = refId;
    }
    public double getAmount(){
        return amount;
    }
    public void setAmount(double amount){
        this.amount = amount;
    }
    public Long getStudentId(){
        return studentId;
    }
    public void setStudentId(Long studentId){
        this.studentId = studentId;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
}
