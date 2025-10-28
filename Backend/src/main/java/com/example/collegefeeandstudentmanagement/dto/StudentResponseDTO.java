package com.example.collegefeeandstudentmanagement.dto;
import java.time.LocalDateTime;

public class StudentResponseDTO {
    private Long id;
    private String rollNo;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String program;
    private LocalDateTime createdAt;
    private StudentFeeResponseDTO studentFee;

    public StudentResponseDTO(){}

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public String getRollNo(){
        return rollNo;
    }
    public void setRollNo(String rollNo){
        this.rollNo =rollNo;
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
    public StudentFeeResponseDTO getStudentFee(){
        return studentFee;
    }
    public void setStudentFee(StudentFeeResponseDTO studentFee){
        this.studentFee = studentFee;
    }
}
