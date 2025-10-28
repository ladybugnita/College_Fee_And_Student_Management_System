package com.example.collegefeeandstudentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

@Entity
@Table(name ="students")

public class Student {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message ="Roll number is required")
    @Pattern(regexp ="\\d+", message="Roll number must be numeric")
    @Column(unique = true, nullable =false)
    private String rollNo;

    @NotBlank(message ="First Name is required")
    @Size(max =50, message ="First Name must be at most 50 characters")
    @Pattern(regexp ="^[A-Za-z]+$", message ="First Name must contain only alphabets")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message ="Last Name is required")
    @Size(max =50, message ="Last Name must be at most 50 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message= "Last Name must contain only alphabets")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message ="Email is required")
    @Email(message = "Email should be valid")
    @Column(unique =true, nullable =false)
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp ="^(\\+\\d{1,3})?\\d{10}$", message = "Phone number must have optional country code")
    private String phone;

    @NotBlank(message ="program is required")
    @Pattern(regexp ="^[A-Za-z ]+$", message = "Program must contain only alphabets and spaces")
    private String program;

    @OneToOne(mappedBy = "student",cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private StudentFee studentFee;

    private LocalDateTime createdAt;

    public Student(){}

    public Student(String rollNo, String firstName, String lastName, String email, String phone, String program){
            this.rollNo =rollNo;
            this.firstName =firstName;
            this.lastName=lastName;
            this.email=email;
            this.phone=phone;
            this.program =program;
            this.createdAt = LocalDateTime.now();
        }
        public Long getId(){return id;}
        public void setId(Long id){this.id =id;}
        public String getRollNo(){return rollNo;}
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
        public StudentFee getStudentFee(){
        return studentFee;
        }
        public void setStudentFee(StudentFee studentFee){
        this.studentFee = studentFee;
        }
        public LocalDateTime getCreatedAt(){
        return createdAt;
        }
        public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
        }

    }

