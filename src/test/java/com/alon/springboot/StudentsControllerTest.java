package com.alon.springboot;

import com.alon.springboot.controller.StudentsController;
import com.alon.springboot.model.objects.StudentOut;
import com.alon.springboot.util.SmsService;
import com.alon.springboot.util.aws.AWSService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.alon.springboot.utils.TestContext;
import com.alon.springboot.utils.TestMocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;


import static com.alon.springboot.utils.StudentSearch.StudentSearchBuilder.aStudentSearch;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;



@SpringBootTest(classes = {SpringbootApplication.class, TestMocks.class})
@ActiveProfiles({"test"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringJUnitConfig
class StudentsControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentsController studentsController;

    @Autowired
    private SmsService smsService;

    @Autowired
    private AWSService awsService;
    private TestContext testContext;

    @BeforeEach
    void initContext() {
        testContext = new TestContext(objectMapper);
        testContext.setStudentController(studentsController);
    }

    @Test
    void get10Patients() throws Exception {
        testContext.givenStudents(10, studentsController);
        assertThat(aStudentSearch(studentsController, testContext.testUuid()).execute().getData().size(), is(10));
    }

    @Test
    void checkSatFromFilter() throws Exception {
        testContext.givenStudents(10, studentsController);
        assertThat(aStudentSearch(studentsController, testContext.testUuid()).fromSatScore(600).execute().getData().size(), is(5));
    }

    @Test
    void checkSatToFilter() throws Exception {
        testContext.givenStudents(10, studentsController);
        assertThat(aStudentSearch(studentsController, testContext.testUuid()).toSatScore(600).execute().getData().size(), is(6));
    }

    @Test
    void checkSmsSent() throws Exception {
        testContext.givenStudents(10, studentsController);
        studentsController.smsAll("hi");
        Thread.sleep(1000);
        verify(smsService, atLeastOnce()).send(any(),any());
    }

    @Test
    void checkPictureUpload() throws Exception {
        testContext.givenStudents(1, studentsController);
        StudentOut student = testContext.getFirstStudent();
        studentsController.uploadStudentImage(student.getId(),  new MockMultipartFile("test", "originalFileName", "png", new byte[0]));
        verify(awsService, times(  1)).putInBucket(any(),any());
    }
}
