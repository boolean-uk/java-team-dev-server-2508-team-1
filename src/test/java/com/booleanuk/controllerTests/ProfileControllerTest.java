package com.booleanuk.controllerTests;

import com.booleanuk.cohorts.controllers.AuthController;
import com.booleanuk.cohorts.controllers.ProfileController;
import com.booleanuk.cohorts.controllers.SearchController;
import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.ERole;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.SignupRequest;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.RoleRepository;
import com.booleanuk.cohorts.repository.UserRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProfileControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthController authController;

    @Autowired
    ProfileController profileController;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CohortRepository cohortRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    private int actualUserId;
    private int testCohortId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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
        ResponseEntity<?> registerResponse = this.authController.registerUser(signupRequest);
        entityManager.flush();
        entityManager.clear();


        actualUserId = userRepository.findAll().get(0).getId();

    }

    @Test
    public void heuristics_testClassSetup() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("searchController"));
    }

    @Test
    public void tryCreateProfile_testFirstNameOnCreatedProfile() throws Exception {
        String profileJson = """
        {
            "userId": %d,
            "first_name": "Thomas",
            "last_name": "Ladder",
            "username": "gottaStepUp",
            "mobile": "244783772",
            "github_username": "tallerThanU",
            "bio": "GI need a ladder, but can't afford one. So, steps will have to be taken",
            "role": "ROLE_STUDENT",
            "specialism": "Big moves",
            "cohort": 1,
            "start_date": "1999-01-01",
            "end_date": "2039-01-01",
            "photo": "https://media.makeameme.org/created/ladder-i.jpg"
        }
        """.formatted(actualUserId);

        MvcResult result = this.mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result);

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json);

        JSONObject profile = jsonObject.getJSONObject("profile");
        String firstName = profile.getString("firstName");
        String username = profile.getString("username");

        System.out.println("First Name: " + firstName);
        assertEquals("Thomas", firstName, "Profile first name should be Thomas");
        assertEquals("gottaStepUp", username, "Profile username should be gottaStepUp");
    }

    @Test
    public void tryGetProfileForId_testFirstNameOnFoundProfile_withProfilesInDB() throws Exception {
        ResponseEntity<?> profileResponse = profileController.createProfile(new ProfileController.PostProfile(
                actualUserId, // Use the actual user ID
                "Thomas",
                "Ladder",
                "gottaStepUp",
                "244783772",
                "tallerThanU",
                "I need a ladder, but can't afford one. So, steps will have to be taken",
                "ROLE_STUDENT",
                "Big moves",
                1,
                "1999-01-01",
                "2039-01-01",
                "https://media.makeameme.org/created/ladder-i.jpg"
        ));

        entityManager.flush();
        entityManager.clear();

        MvcResult result = this.mockMvc.perform(get("/profiles/"+ actualUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        assertNotNull(result);

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json);

        JSONObject profile = jsonObject.getJSONObject("data").getJSONObject("profile");
        String firstName = profile.getString("firstName");
        String username = profile.getString("username");
        System.out.println("First Name: " + firstName);
        assertEquals("Thomas", firstName, "Profile first name should be Thomas");
        assertEquals("gottaStepUp", username, "Profile first name should be Thomas");
    }

}
