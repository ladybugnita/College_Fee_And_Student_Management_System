package com.example.collegefeeandstudentmanagement.service;

import com.example.collegefeeandstudentmanagement.dto.UpdateStudentDTO;
import com.example.collegefeeandstudentmanagement.entity.Student;
import com.example.collegefeeandstudentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository repository;
    public StudentService(StudentRepository repository){
        this.repository = repository;
    }

    public Student createStudent(Student student){
        student.setCreatedAt(LocalDateTime.now());
        return repository.save(student);
    }
    public List<Student>getAllStudents(){
        return repository.findAll();
    }
    public Optional<Student> getStudentById(Long id){
        return repository.findById(id);
    }
    public Student updateStudent(Long id, Student updatedStudent){
        Optional<Student> optionalStudent = repository.findById(id);
        if(optionalStudent.isPresent()){
            Student existingStudent = optionalStudent.get();
            existingStudent.setFirstName(updatedStudent.getFirstName());
            existingStudent.setLastName(updatedStudent.getLastName());
            existingStudent.setEmail(updatedStudent.getEmail());
            existingStudent.setPhone(updatedStudent.getPhone());
            existingStudent.setProgram(updatedStudent.getProgram());
            existingStudent.setRollNo(updatedStudent.getRollNo());
            return repository.save(existingStudent);
        }
        return null;
    }
    public Optional<Student> updateStudent(Long id, UpdateStudentDTO dto){
        Optional<Student> optionalStudent = repository.findById(id);
        if(optionalStudent.isEmpty()) return Optional.empty();
        Student student = optionalStudent.get();
        if(dto.getRollNo() != null)
            student.setRollNo(dto.getRollNo());
        if(dto.getFirstName() != null)
            student.setFirstName(dto.getFirstName());
        if(dto.getLastName() != null)
            student.setLastName(dto.getLastName());
        if(dto.getEmail() != null)
            student.setEmail(dto.getEmail());
        if(dto.getPhone() != null)
            student.setPhone(dto.getPhone());
        if(dto.getProgram()!= null)
            student.setProgram(dto.getProgram());
        repository.save(student);
        return Optional.of(student);
    }
    public boolean deleteStudent(Long id){
        if(repository.existsById(id)){
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}