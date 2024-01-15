package com.alon.springboot.controller;

import com.alon.springboot.model.*;
import com.alon.springboot.model.objects.Student;
import com.alon.springboot.model.objects.StudentIn;
import com.alon.springboot.model.objects.StudentOut;
import com.alon.springboot.repository.StudentService;
import com.alon.springboot.util.SmsService;
import com.alon.springboot.util.aws.AWSService;
import com.alon.springboot.util.exceptions.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

import static com.alon.springboot.util.Dates.atUtc;
import static com.alon.springboot.util.FPS.FPSBuilder.aFPS;
import static com.alon.springboot.util.FPSCondition.FPSConditionBuilder.aFPSCondition;
import static com.alon.springboot.util.FPSField.FPSFieldBuilder.aFPSField;
import static com.alon.springboot.util.Strings.likeLowerOrNull;

@RestController
@RequestMapping("/api/students")
public class StudentsController {

    @Autowired
    StudentService studentService;

    /**
     * In Spring Boot, the EntityManager is a core component of the Java Persistence API (JPA).
     * JPA is a standard specification for object-relational mapping (ORM) in Java, and it is used for managing relational data in a Java application.
     * The EntityManager interface is part of the JPA specification ,and it provides a set of APIs for performing CRUD (Create, Read, Update, Delete) operations on entities.
     */
    @Autowired
    EntityManager em;//Work with the DB

    /**
     * ObjectMapper is a class provided by the Jackson library, which is a popular Java library for processing JSON data.
     * Jackson provides a set of powerful and flexible APIs for reading and writing JSON data in Java.
     * The ObjectMapper class is a central part of Jackson and is used to map between Java objects and JSON data.
     */
    @Autowired
    ObjectMapper om;//Convert objects to and from JSON to Java objects

    @Autowired
    AWSService awsService;

    @Autowired
    SmsService smsService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<PaginationAndList> search(@RequestParam(required = false) String fullName,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromBirthDate,
                                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toBirthDate,
                                                    @RequestParam(required = false) Integer fromSatScore,
                                                    @RequestParam(required = false) Integer toSatScore,
                                                    @RequestParam(required = false) Integer fromAvgScore,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "50") @Min(1) Integer count,
                                                    @RequestParam(defaultValue = "id") StudentSortField sort,
                                                    @RequestParam(defaultValue = "asc") SortDirection sortDirection) throws JsonProcessingException {

        var res =aFPS().select(List.of(
                        aFPSField().field("s.id").alias("id").build(),
                        aFPSField().field("s.created_at").alias("createdat").build(),
                        aFPSField().field("s.fullname").alias("fullname").build(),
                        aFPSField().field("s.birth_date").alias("birthdate").build(),
                        aFPSField().field("s.sat_score").alias("satscore").build(),
                        aFPSField().field("s.graduation_score").alias("graduationscore").build(),
                        aFPSField().field("s.phone").alias("phone").build(),
                        aFPSField().field("s.profile_picture").alias("profilepicture").build(),
                        aFPSField().field("(select avg(sg.course_score) from  student_grade sg where sg.student_id = s.id ) ").alias("avgscore").build()
                ))
                .from(List.of(" student s"))
                .conditions(List.of(
                        aFPSCondition().condition("( lower(fullname) like :fullName )").parameterName("fullName").value(likeLowerOrNull(fullName)).build(),
                        aFPSCondition().condition("( s.birth_Date >= :fromBirthDate )").parameterName("fromBirthDate").value(atUtc(fromBirthDate)).build(),
                        aFPSCondition().condition("( s.birth_Date <= :toBirthDate )").parameterName("toBirthDate").value(atUtc(toBirthDate)).build(),
                        aFPSCondition().condition("( sat_score >= :fromSatScore )").parameterName("fromSatScore").value(fromSatScore).build(),
                        aFPSCondition().condition("( sat_score <= :toSatScore )").parameterName("toSatScore").value(toSatScore).build(),
                        aFPSCondition().condition("( (select avg(sg.course_score) from  student_grade sg where sg.student_id = s.id ) >= :fromAvgScore )").parameterName("fromAvgScore").value(fromAvgScore).build()
                )).sortField(sort.fieldName).sortDirection(sortDirection).page(page).count(count)
                .itemClass(StudentOut.class)
                .build().exec(em, om);
        return ResponseEntity.ok(res);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getOneStudent(@PathVariable Long id)
    {
        return new ResponseEntity<>(studentService.findById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/highSat", method = RequestMethod.GET)
    public ResponseEntity<?> getHighSatStudents(@RequestParam Integer sat)
    {
        return new ResponseEntity<>(studentService.getStudentWithSatHigherThan(sat), HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> insertStudent(@RequestBody StudentIn studentIn)
    {
        Student student = studentIn.toStudent();
        student = studentService.save(student);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @RequestMapping(value = "/{studentId}", method = RequestMethod.PUT)
    public ResponseEntity<?> updateStudent(@PathVariable Long studentId, @RequestBody StudentIn student)
    {
        Optional<Student> dbStudent = studentService.findById(studentId);
        if (dbStudent.isEmpty())
            throw new CustomException("Student with id: " + studentId + " not found");

        student.updateStudent(dbStudent.get());
        Student updatedStudent = studentService.save(dbStudent.get());
        return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteStudent(@PathVariable Long id)
    {
        Optional<Student> dbStudent = studentService.findById(id);
        if (dbStudent.isEmpty())
            throw new CustomException("Student with id: " + id + " not found");
        studentService.delete(dbStudent.get());
        return new ResponseEntity<>("DELETED", HttpStatus.OK);
    }

    @RequestMapping(value = "/{studentId}/image", method = RequestMethod.PUT)
    public ResponseEntity<?> uploadStudentImage(@PathVariable Long studentId,  @RequestParam("image") MultipartFile image)
    {
        Optional<Student> dbStudent = studentService.findById(studentId);
        if (dbStudent.isEmpty())
            throw new CustomException("Student with id: " + studentId + " not found");

        String bucketPath = "apps/alon/student-" +  studentId + ".png" ;
        awsService.putInBucket(image, bucketPath);
        dbStudent.get().setProfilePicture(bucketPath);
        Student updatedStudent = studentService.save(dbStudent.get());
        return new ResponseEntity<>(StudentOut.of(updatedStudent, awsService) , HttpStatus.OK);
    }

    @RequestMapping(value = "/sms/all", method = RequestMethod.POST)
    public ResponseEntity<?> smsAll(@RequestParam String text)
    {
        new Thread(()-> {
            IteratorUtils.toList(studentService.all().iterator())
                    .parallelStream()
                    .map(Student::getPhone)
                    .filter(phone -> !Strings.isEmpty(phone))
                    .forEach(phone -> smsService.send(text, phone));
        }).start();
        return new ResponseEntity<>("SENDING", HttpStatus.OK);
    }

}
