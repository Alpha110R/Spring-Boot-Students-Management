package com.alon.springboot.controller;

import com.alon.springboot.model.objects.GradeIn;
import com.alon.springboot.model.objects.Student;
import com.alon.springboot.model.objects.StudentGrade;
import com.alon.springboot.repository.StudentGradeService;
import com.alon.springboot.repository.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/students")
public class StudentsGradesController {
    @Autowired
    StudentService studentService;

    @Autowired
    StudentGradeService studentGradeService;

    @RequestMapping(value = "/{studentId}/grades", method = RequestMethod.POST)
    public ResponseEntity<?> insertStudentGrade(Long studentId,  @RequestBody GradeIn gradeIn)
    {
        var student = studentService.findById(studentId);
        if (student.isEmpty())
            throw new RuntimeException("Student:" + studentId +" not found");

        StudentGrade studentGrade = gradeIn.toGrade(student.get());
        studentGrade = studentGradeService.save(studentGrade);
        return new ResponseEntity<>(studentGrade, HttpStatus.OK);
    }

    @RequestMapping(value = "/{studentId}/grades/{gradeId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateStudent(@PathVariable Long studentId, @PathVariable Long gradeId, @RequestBody GradeIn gradeIn)
    {
        Optional<Student> dbStudent = studentService.findById(studentId);
        if (dbStudent.isEmpty())
            throw new RuntimeException("Student with id: " + studentId + " not found");

        Optional<StudentGrade> dbStudentGrade = studentGradeService.findById(gradeId);
        if (dbStudentGrade.isEmpty())
            throw new RuntimeException("Student grade with id: " + gradeId + " not found");

        gradeIn.updateStudentGrade(dbStudentGrade.get());
        StudentGrade updatedStudentGrade = studentGradeService.save(dbStudentGrade.get());
        return new ResponseEntity<>(updatedStudentGrade, HttpStatus.OK);
    }

    @RequestMapping(value = "/{studentId}/grades/{gradeId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteStudentGrade(@PathVariable Long studentId, @PathVariable Long gradeId)
    {
        Optional<Student> dbStudent = studentService.findById(studentId);
        if (dbStudent.isEmpty())
            throw new RuntimeException("Student with id: " + studentId + " not found");

        Optional<StudentGrade> dbStudentGrade = studentGradeService.findById(gradeId);
        if (dbStudentGrade.isEmpty())
            throw new RuntimeException("Student grade with id: " + gradeId + " not found");

        studentGradeService.delete(dbStudentGrade.get());
        return new ResponseEntity<>("DELETED", HttpStatus.OK);
    }
}
