package com.alon.springboot.repository;


import com.alon.springboot.model.objects.StudentGrade;
import org.springframework.data.repository.CrudRepository;

public interface StudentGradeRepository extends CrudRepository<StudentGrade,Long> {

}
