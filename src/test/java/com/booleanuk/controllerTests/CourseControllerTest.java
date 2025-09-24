package com.booleanuk.controllerTests;

import com.booleanuk.cohorts.controllers.AuthController;
import com.booleanuk.cohorts.controllers.ProfileController;
import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.request.SignupRequest;
import com.booleanuk.cohorts.repository.*;
import com.booleanuk.cohorts.security.services.UserDetailsImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CourseControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    AuthController authController;

    @Autowired
    ProfileController profileController;

    @Autowired
    ProfileRepository profileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    private int testCourseId;
    private int testCohortId;
    private User testTeacherUser;
    private User testStudentUser;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        profileRepository.deleteAll();
        cohortRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        Role teacherRole = new Role(ERole.ROLE_TEACHER);
        Role studentRole = new Role(ERole.ROLE_STUDENT);
        roleRepository.save(teacherRole);
        roleRepository.save(studentRole);
        entityManager.flush();

        Course testCourse = new Course();
        testCourse.setName("Java Development");
        testCourse.setStartDate(LocalDate.parse("2024-01-01"));
        testCourse.setEndDate(LocalDate.parse("2024-06-01"));
        testCourse = courseRepository.save(testCourse);
        testCourseId = testCourse.getId();
        entityManager.flush();

        Cohort testCohort = new Cohort();
        testCohort.setName("Java Cohort 1");
        testCohort.setCourse(testCourse);
        testCohort = cohortRepository.save(testCohort);
        testCohortId = testCohort.getId();
        entityManager.flush();

        SignupRequest teacherSignupRequest = new SignupRequest("teacher@test.com", "@Teacher123");
        this.authController.registerUser(teacherSignupRequest);
        entityManager.flush();

        testTeacherUser = userRepository.findByEmail("teacher@test.com").orElse(null);

        ProfileController.PostProfile teacherPostProfile = new ProfileController.PostProfile(
                testTeacherUser.getId(),
                "John",
                "Teacher",
                "johnTeacher",
                "123456789",
                "teacherGitHub",
                "I am a teacher",
                "ROLE_TEACHER",
                "Teaching Java",
                testCohortId,
                "1980-01-01",
                "2030-01-01",
                "https://example.com/teacher.jpg"
        );
        this.profileController.createProfile(teacherPostProfile);
        entityManager.flush();

        SignupRequest studentSignupRequest = new SignupRequest("student@test.com", "@Student123");
        this.authController.registerUser(studentSignupRequest);
        entityManager.flush();

        testStudentUser = userRepository.findByEmail("student@test.com").orElse(null);

        ProfileController.PostProfile studentPostProfile = new ProfileController.PostProfile(
                testStudentUser.getId(),
                "Jane",
                "Student",
                "janeStudent",
                "987654321",
                "studentGitHub",
                "I am a student",
                "ROLE_STUDENT",
                "Learning Java",
                testCohortId,
                "2000-01-01",
                "2040-01-01",
                "https://example.com/student.jpg"
        );
        this.profileController.createProfile(studentPostProfile);
        entityManager.flush();
        entityManager.clear();

        testTeacherUser = userRepository.findById(testTeacherUser.getId()).orElse(null);
        testStudentUser = userRepository.findById(testStudentUser.getId()).orElse(null);
    }

    private void authenticateUser(User user) {
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void heuristics_testClassSetup() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("courseController"));
    }

    @Test
    public void tryGetAllCourses_testCourseNameAndDates_withSingleCourseInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/courses")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
        JSONArray courses = response.getJSONArray("courses");
        assertNotNull(courses);
        assertEquals(1, courses.length());

        JSONObject firstCourse = courses.getJSONObject(0);
        assertEquals("Java Development", firstCourse.getString("name"));
        assertEquals("2024-01-01", firstCourse.getString("startDate"));
        assertEquals("2024-06-01", firstCourse.getString("endDate"));
    }

    @Test
    public void tryGetCourseById_testCourseDetailsAndCohorts_withSingleCourseInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/courses/" + testCourseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("course");
        assertNotNull(response);

        assertEquals("Java Development", response.getString("name"));
        assertEquals("2024-01-01", response.getString("startDate"));
        assertEquals("2024-06-01", response.getString("endDate"));

        JSONArray cohorts = response.getJSONArray("cohorts");
        assertEquals(1, cohorts.length());
        assertEquals("Java Development", response.getString("name"));
    }

    @Test
    public void tryGetCourseById_testNotFound_withInvalidId() throws Exception {
        this.mockMvc.perform(get("/courses/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void tryGetAllStudents_testStudentProfilesInCourse_withStudentInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/courses/students/" + testCourseId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
        JSONArray profiles = response.getJSONArray("profiles");
        assertNotNull(profiles);
        assertEquals(1, profiles.length());

        JSONObject studentProfile = profiles.getJSONObject(0);
        assertEquals("Jane", studentProfile.getString("firstName"));
        assertEquals("Student", studentProfile.getString("lastName"));
        assertEquals("janeStudent", studentProfile.getString("username"));
    }

    @Test
    public void tryGetAllStudents_testNotFound_withInvalidCourseId() throws Exception {
        this.mockMvc.perform(get("/courses/students/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void tryCreateCourse_testCourseCreation_withValidData() throws Exception {
        String requestBody = """
                {
                    "name": "Python Development",
                    "startDate": "2024-07-01",
                    "endDate": "2024-12-01"
                }
                """;

        MvcResult result = this.mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("course");
        assertNotNull(response);

        assertEquals("Python Development", response.getString("name"));
        assertEquals("2024-07-01", response.getString("startDate"));
        assertEquals("2024-12-01", response.getString("endDate"));

        Course savedCourse = courseRepository.findById(response.getInt("id")).orElse(null);
        assertNotNull(savedCourse);
        assertEquals("Python Development", savedCourse.getName());
    }

    @Test
    public void tryCreateCourse_testBadRequest_withBlankDates() throws Exception {
        String requestBody = """
                {
                    "name": "Invalid Course",
                    "startDate": "",
                    "endDate": ""
                }
                """;

        this.mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}