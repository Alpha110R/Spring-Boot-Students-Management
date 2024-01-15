package com.alon.springboot.repository;

import com.alon.springboot.model.objects.Student;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentRepository extends CrudRepository<Student,Long> {

    List<Student> findAllBySatScoreGreaterThan(Integer satScore);

}
