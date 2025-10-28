package com.example.collegefeeandstudentmanagement.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class UpdateStudentDTO {
    @Pattern(regexp ="\\d+", message = "Roll number must contain only digits")
    private String rollNo;
    @Size(max =50, message ="firstName can be max 50 characters")
    @Pattern(regexp ="^[A-Za-z]+$", message = "firstName must contain only alphabets")
    private String firstName;
    @Size(max=50, message="lastName can be max 50 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message = "lastName must contain only alphabets")
    private String lastName;
    @Email(message ="Email should be valid")
    private String email;
    @Pattern(regexp = "^(\\+\\d{1,3})?\\d{10}$", message = "phone number must have optional code.")
    private String phone;
    @Pattern(regexp ="^[A-Za-z ]+$", message ="program must contain only letters and spaces")
    private String program;
    private LocalDateTime createdAt;

    public String getRollNo(){
        return rollNo;
    }
    public void setRollNo(String rollNo){
        this.rollNo=rollNo;
    }
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public String getLastName(){
        return lastName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email =email;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public String getProgram(){
        return program;
    }
    public void setProgram(String program){
        this.program = program;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }
}
