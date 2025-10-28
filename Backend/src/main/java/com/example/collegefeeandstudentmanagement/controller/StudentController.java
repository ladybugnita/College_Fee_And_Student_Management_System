package com.example.collegefeeandstudentmanagement.controller;

import com.example.collegefeeandstudentmanagement.dto.StudentFeeResponseDTO;
import com.example.collegefeeandstudentmanagement.dto.StudentResponseDTO;
import com.example.collegefeeandstudentmanagement.dto.UpdateStudentDTO;
import com.example.collegefeeandstudentmanagement.entity.Student;
import com.example.collegefeeandstudentmanagement.repository.StudentRepository;
import com.example.collegefeeandstudentmanagement.service.StudentFeeMapperService;
import com.example.collegefeeandstudentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/api/students")

public class StudentController {
    private final StudentRepository studentRepository;
    private final StudentService studentService;
    private final StudentFeeMapperService studentFeeMapperService;

    public StudentController(StudentRepository studentRepository, StudentService studentService, StudentFeeMapperService studentFeeMapperService) {
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.studentFeeMapperService = studentFeeMapperService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        Student saved = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> getStudent(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();

        if (isStudentTryingToAccessOtherStudent(student, username, auth)){
            throw new AccessDeniedException("Access Denied: You can only access your own data");
        }
        return ResponseEntity.ok(mapToDTO(student));
    }
    private boolean isStudentTryingToAccessOtherStudent(Student student, String username,Authentication auth){
        return auth.getAuthorities().stream()
                .anyMatch(a-> a.getAuthority().equals("ROLE_USER"))
                && !student.getEmail().equals(username);
    }
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<StudentResponseDTO> getLoggedInStudent(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return studentRepository.findByEmail(username)
                .map(this::mapToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody Student student) {
        Student updated = studentService.updateStudent(id, student);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteStudent (@PathVariable Long id){
            if (studentService.deleteStudent(id)) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        }
     @PreAuthorize("hasRole('ADMIN')")
     @PatchMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStudentDTO dto){
        Optional<Student> updated = studentService.updateStudent(id, dto);
        return updated
                .map(student -> ResponseEntity.ok(student))
                .orElse(ResponseEntity.notFound().build());
    }
    private StudentResponseDTO mapToDTO(Student student){
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setId(student.getId());
        dto.setRollNo(student.getRollNo());
        dto.setFirstName(student.getFirstName());
        dto.setLastName(student.getLastName());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setEmail(student.getEmail());
        dto.setPhone(student.getPhone());
        dto.setProgram(student.getProgram());
        dto.setCreatedAt(student.getCreatedAt());
        if(student.getStudentFee() != null){
            try{
                StudentFeeResponseDTO feeDTO = studentFeeMapperService.toDTO(student.getStudentFee());
                dto.setStudentFee(feeDTO);
            }
            catch(Exception e){
                System.err.println("Error mapping student fee for student" + student.getId() + ":" +
                        e.getMessage());
                dto.setStudentFee(null);
            }
        }
        return dto;
    }
}
