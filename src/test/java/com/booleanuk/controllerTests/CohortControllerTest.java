package com.booleanuk.controllerTests;

import com.booleanuk.cohorts.controllers.AuthController;
import com.booleanuk.cohorts.controllers.ProfileController;
import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.ERole;
import com.booleanuk.cohorts.payload.request.SignupRequest;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import com.booleanuk.cohorts.repository.RoleRepository;
import com.booleanuk.cohorts.repository.CohortRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CohortControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CohortRepository cohortRepository;

    @Autowired
    AuthController authController;

    @Autowired
    ProfileController profileController;

    @Autowired
    ProfileRepository profileRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    private int actualUserId;
    private int testCohortId;

    private User testUser;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        profileRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        cohortRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();


        Role teacherRole = new Role(ERole.ROLE_TEACHER);
        Role studentRole = new Role(ERole.ROLE_STUDENT);
        roleRepository.save(teacherRole);
        roleRepository.save(studentRole);
        entityManager.flush();


        Cohort testCohort = new Cohort();
        testCohort = cohortRepository.save(testCohort);
        testCohortId = testCohort.getId();
        entityManager.flush();

        SignupRequest signupRequest = new SignupRequest("thomas@ladder.com", "@Qwerty12345");
        this.authController.registerUser(signupRequest);
        entityManager.flush();

        testUser = userRepository.findAll().get(0);
        actualUserId = testUser.getId();

        ProfileController.PostProfile postProfile = new ProfileController.PostProfile(
                actualUserId,
                "Thomas",
                "Ladder",
                "gottaStepUp",
                "244783772",
                "tallerThanU",
                "I need a ladder, but can't afford one. So, steps will have to be taken",
                "ROLE_TEACHER",
                "Big moves",
                testCohortId,
                "1999-01-01",
                "2039-01-01",
                "https://media.makeameme.org/created/ladder-i.jpg"
        );
        ResponseEntity profileRegisterResponse = this.profileController.createProfile(postProfile);
        entityManager.flush();
        entityManager.clear();

        // Refresh the user entity to get the updated state with profile
        testUser = userRepository.findById(actualUserId).orElse(null);
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
        assertNotNull(webApplicationContext.getBean("cohortController"));
    }

    @Test
    public void tryGetAllCohorts_testFirstNameOnFirstProfile_withSingleProfileInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/cohorts")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
        JSONArray cohorts = response.getJSONArray("cohorts");
        assertNotNull(cohorts);

        JSONObject firstProfile = cohorts.getJSONObject(0).getJSONArray("profiles").getJSONObject(0);
        assertEquals(firstProfile.getString("firstName"), "Thomas");
    }

    @Test
    public void tryGetCohortsById_testEmailOnFirstProfile_withSingleProfileInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/cohorts/" + testCohortId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("cohort");
        assertNotNull(response);

        JSONObject firstProfile = response.getJSONArray("profiles").getJSONObject(0);
        assertEquals(firstProfile.getString("firstName"), "Thomas");

    }

    @Test
    public void tryGetCohortsByUserId_testEmailOnFirstProfile_withSingleProfileInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/cohorts/teacher/" + actualUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("cohort");
        assertNotNull(response);

        JSONObject firstProfile = response.getJSONArray("profiles").getJSONObject(0);
        assertEquals(firstProfile.getString("firstName"), "Thomas");
        assertEquals(firstProfile.getJSONObject("user").getString("email"), "thomas@ladder.com");
    }

    @Test
    public void tryAddStudentToCohort_checkProfileObjectAndResponseBody_withSingleProfileInDb() throws Exception {
        Cohort secondTestCohort = new Cohort();
        secondTestCohort = cohortRepository.save(secondTestCohort);
        int secondTestCohortId = secondTestCohort.getId();
        entityManager.flush();

        SignupRequest studentSignupRequest = new SignupRequest("student@test.com", "@Student123");
        this.authController.registerUser(studentSignupRequest);
        entityManager.flush();

        User studentUser = userRepository.findByEmail("student@test.com").orElse(null);
        assertNotNull(studentUser);

        ProfileController.PostProfile studentPostProfile = new ProfileController.PostProfile(
                studentUser.getId(),
                "Fritjof",
                "Ladderson",
                "BigLadderMan",
                "748337483784",
                "bigLadderMan",
                "I invented the upside down ladder",
                "ROLE_STUDENT",
                "Alternative ladders",
                secondTestCohortId,
                "1999-01-01",
                "2040-01-01",
                "https://example.com/ladder.jpg"
        );
        this.profileController.createProfile(studentPostProfile);
        entityManager.flush();

        Profile studentProfile = profileRepository.findById(studentUser.getId()).orElse(null);
        assertNotNull(studentProfile);

        authenticateUser(testUser);

        String requestBody = "{\"profileId\":" + studentProfile.getId() + "}";

        MvcResult result = this.mockMvc.perform(patch("/cohorts/teacher/" + testCohortId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertNotNull(response);

        assertEquals(testCohortId, response.getJSONObject("cohort").getInt("id"));
        assertEquals("Fritjof", response.getString("firstName"));
        assertEquals("Ladderson", response.getString("lastName"));

        Profile updatedProfile = profileRepository.findById(studentProfile.getId()).orElse(null);
        assertNotNull(updatedProfile);
        assertNotNull(updatedProfile.getCohort());
        assertEquals(testCohortId, updatedProfile.getCohort().getId());
    }

}
