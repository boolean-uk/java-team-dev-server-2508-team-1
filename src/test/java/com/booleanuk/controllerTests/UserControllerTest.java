package com.booleanuk.controllerTests;

import com.booleanuk.cohorts.controllers.*;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.PostRequest;
import com.booleanuk.cohorts.payload.request.SignupRequest;
import com.booleanuk.cohorts.payload.response.PostResponse;
import com.booleanuk.cohorts.repository.PostRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserController userController;

    @Autowired
    private AuthController authController;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostController postController;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    private int actualUserId;
    private User testUser;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        userRepository.deleteAll();

        SignupRequest signupRequest = new SignupRequest("thomas@ladder.com", "@Qwerty12345");
        ResponseEntity<?> registerResponse = this.authController.registerUser(signupRequest);
        entityManager.flush();
        entityManager.clear();

        testUser = userRepository.findAll().get(0);
        actualUserId = testUser.getId();
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
        assertNotNull(webApplicationContext.getBean("userController"));
    }

    @Test
    public void tryGetAllUsers_testEmailOnFirstUser_withSingleUserInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result);
        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("data");

        JSONArray jsonArray = jsonObject.getJSONArray("users");
        String email = jsonArray.getJSONObject(0).getString("email");

        assertTrue(jsonArray.length() == 1);
        assertEquals("thomas@ladder.com", email, "Email should be thomas@ladder.com");
    }

    @Test
    public void tryGetAllUsers_testEmailOnMultipleUsers_withMultipleUserInDb() throws Exception {
        this.authController.registerUser(new SignupRequest("fredrik@ladder.com", "@Qwerty12345"));
        this.authController.registerUser(new SignupRequest("sara@ladder.com", "@Qwerty12345"));
        this.authController.registerUser(new SignupRequest("josefine@ladder.com", "@Qwerty12345"));
        entityManager.flush();
        entityManager.clear();

        MvcResult result = this.mockMvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result);

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("data");
        JSONArray jsonArray = jsonObject.getJSONArray("users");

        assertTrue(jsonArray.length() == 4);

        String emailThomas = jsonArray.getJSONObject(0).getString("email");
        String emailFredrik = jsonArray.getJSONObject(1).getString("email");
        String emailSara = jsonArray.getJSONObject(2).getString("email");
        String emailJosefine = jsonArray.getJSONObject(3).getString("email");

        assertEquals("thomas@ladder.com", emailThomas, "Email should be thomas@ladder.com");
        assertEquals("fredrik@ladder.com", emailFredrik, "Email should be fredrik@ladder.com");
        assertEquals("sara@ladder.com", emailSara, "Email should be sara@ladder.com");
        assertEquals("josefine@ladder.com", emailJosefine, "Email should be josefine@ladder.com");
    }

    @Test
    public void tryGetUserById_testEmailOnFirstUser_withSingleUserInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/users/" + actualUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result);
        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("data");

        JSONObject user = jsonObject.getJSONObject("user");
        String email = user.getString("email");
        assertEquals("thomas@ladder.com", email, "Email should be thomas@ladder.com");
    }

    @Test
    public void tryDeleteUserById_testReturnCodeAndIfUserIsActuallyDeleted() throws Exception {
        MvcResult result = this.mockMvc.perform(delete("/users/" + actualUserId)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(userRepository.findById(actualUserId).isEmpty(), "User list should be empty");
    }

    @Test
    public void tryUpdateLikedPosts_testAddingSingleLikedPost_checkUserObjectAndResponseBody_withSingleUserIndb() throws Exception {
        authenticateUser(testUser);

        ResponseEntity postResponse = this.postController.createPost(new PostRequest("It's not DNS... There's no way it's DNS... It was DNS", actualUserId));

        entityManager.flush();
        entityManager.clear();

        int postId = this.postRepository.findAll().get(0).getId();

        // Create the request body with the post_id
        String requestBody = "{\"post_id\": " + postId + "}";

        MvcResult result = this.mockMvc.perform(patch("/users/" + actualUserId)
                .with(user(UserDetailsImpl.build(testUser)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("data");
        JSONObject user = jsonObject.getJSONObject("user");
        JSONArray likedPosts = user.getJSONArray("likedPosts");

        assertEquals(1, likedPosts.length(), "User should have 1 liked post in their likedPosts array");
        assertEquals("It's not DNS... There's no way it's DNS... It was DNS",  likedPosts.getJSONObject(0).getString("content"));

        User userWithLike = userRepository.getReferenceById(actualUserId);
        assertTrue(userWithLike.getLikedPosts().size() == 1, "User should have 1 liked post in their likedPosts array");
    }

    @Test
    public void tryUpdateLikedPosts_testAddingMultipleLikedPost_checkUserObjectAndResponseBody_withSingleUserIndb() throws Exception {
        authenticateUser(testUser);

        ResponseEntity postResponse = this.postController.createPost(new PostRequest("It's not DNS... There's no way it's DNS... It was DNS", actualUserId));
        ResponseEntity postResponse2 = this.postController.createPost(new PostRequest("Sorry I forgot", actualUserId));
        entityManager.flush();
        entityManager.clear();

        // Add first post
        int postId = this.postRepository.findAll().get(0).getId();
        String requestBody = "{\"post_id\": " + postId + "}";

        MvcResult result = this.mockMvc.perform(patch("/users/" + actualUserId)
                        .with(user(UserDetailsImpl.build(testUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Add second post
        postId = this.postRepository.findAll().get(1).getId();
        requestBody = "{\"post_id\": " + postId + "}";

        result = this.mockMvc.perform(patch("/users/" + actualUserId)
                        .with(user(UserDetailsImpl.build(testUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // Actual testing logic
        String json = result.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("data");
        JSONObject user = jsonObject.getJSONObject("user");
        JSONArray likedPosts = user.getJSONArray("likedPosts");

        assertEquals(2, likedPosts.length(), "User should have 1 liked post in their likedPosts array");
        assertEquals("It's not DNS... There's no way it's DNS... It was DNS",  likedPosts.getJSONObject(0).getString("content"));
        assertEquals("Sorry I forgot",  likedPosts.getJSONObject(1).getString("content"));

        User userWithLike = userRepository.getReferenceById(actualUserId);
        assertTrue(userWithLike.getLikedPosts().size() == 2, "User should have 1 liked post in their likedPosts array");
    }
}
