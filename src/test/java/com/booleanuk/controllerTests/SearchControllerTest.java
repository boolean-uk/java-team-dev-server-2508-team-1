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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SearchControllerTest {

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
        ResponseEntity<?> registerResponse = this.authController.registerUser(signupRequest);
        entityManager.flush();
        entityManager.clear();

        List<User> users = userRepository.findAll();
        User createdUser = users.get(0);
        int actualUserId = createdUser.getId();

        System.out.println("Using user ID for profile creation: " + actualUserId);


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

    }

    @Test
    public void heuristics_testClassSetup() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("searchController"));
    }

    @Test
    public void trySearchProfilesDefault_withProfilesInDB() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/search/profiles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("profiles");

        assertTrue(jsonArray.length() == 1, "Should return at least one profile");
    }

    @Test
    public void trySearchProfilesQuery_testFirstNameOnFirstFoundProfile_withProfilesInDB() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/search/profiles/thomas"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        JSONObject jsonObject = new JSONObject(json);
        JSONArray profileArray = jsonObject.getJSONObject("data").getJSONArray("profiles");
        assertTrue(profileArray.length() > 0, "Should return at least one profile");

        JSONObject firstProfile = profileArray.getJSONObject(0);
        String profileFirstName = firstProfile.getString("firstName");
        assertTrue(profileFirstName.toLowerCase().contains("thomas"),
                "Profile first name should contain 'thomas'");
    }

    @Test
    public void trySearchProfileQuery_testNoProfilesFound_withProfilesInDB() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/search/profiles/firstnamethatdoesnotexsist"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json);

        JSONArray profileArray = jsonObject.getJSONObject("data").getJSONArray("profiles");
        assertTrue(profileArray.length() == 0, "Should return no profiles");
    }
}
