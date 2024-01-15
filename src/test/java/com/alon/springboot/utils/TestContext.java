package com.alon.springboot.utils;

import com.alon.springboot.controller.StudentsController;
import com.alon.springboot.model.objects.StudentIn;
import com.alon.springboot.model.objects.StudentOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestContext {

    private final String userCreate = UUID.randomUUID().toString();

    private final Map<String, String> vars = new HashMap<>();

    private final ObjectMapper objectMapper;

    private final Principal principal;

    private StudentsController studentsController;

    public TestContext(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
        vars.put("fullName", "");
        vars.put("phone", "");
        vars.put("satScore", "");
        vars.put("graduationScore", "");
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userCreate);
    }

    public void givenStudents(int numStudents, StudentsController studentsController) throws Exception {

        for (int i = 0; i < numStudents; i++) {
            vars.put("fullName", "Student-" + testUuid() + Strings.padStart(Integer.toString(i), 5, '0'));
            vars.put("phone", i % 2 == 0 ? "" : "052523645" + String.valueOf(i));
            vars.put("satScore",  String.valueOf(500 + 20 * i));
            vars.put("graduationScore", String.valueOf(70 + 2 * i));
            studentsController.insertStudent(get("json/student.json", StudentIn.class));
        }
    }

    public StudentOut getFirstStudent() throws Exception {
        return (StudentOut)search().execute().getData().get(0);
    }

    public StudentSearch.StudentSearchBuilder search() {
        return StudentSearch.StudentSearchBuilder.aStudentSearch(studentsController, testUuid());
    }

    private String populate(String source) {
        StrSubstitutor sub = new StrSubstitutor(vars, "{{", "}}");
        return sub.replace(source);
    }

    public <T> T get(String jsonFile, Class<T> clazz) throws Exception {
        String json = readFile(ClassLoader.getSystemResource(jsonFile).toURI());
        String populatedJson = populate(json);
        return objectMapper.readValue(populatedJson, clazz);
    }

    public void setStudentController(StudentsController sc) {
        this.studentsController = sc;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public String testUuid() {
        return userCreate;
    }

    public Principal getUser() {
        return principal;
    }

    private static String readFile(URI filePath) {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

}
