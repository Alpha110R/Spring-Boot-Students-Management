package com.alon.springboot.repository;


import com.alon.springboot.model.objects.StudentGrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StudentGradeService {

    @Autowired
    StudentGradeRepository repository;

    public Iterable<StudentGrade> all() {
        return repository.findAll();
    }

    public Optional<StudentGrade> findById(Long id) {
        return repository.findById(id);
    }


    public StudentGrade save(StudentGrade studentGrade) {
        return repository.save(studentGrade);
    }

    public void delete(StudentGrade studentGrade) {
        repository.delete(studentGrade);
    }

}
