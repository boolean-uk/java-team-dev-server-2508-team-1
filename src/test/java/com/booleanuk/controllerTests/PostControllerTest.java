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
public class PostControllerTest {
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
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

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
    private int testPostId;
    private int testCommentId;
    private User testUser;
    private User testUser2;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        commentRepository.deleteAll();
        postRepository.deleteAll();
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

        SignupRequest signupRequest = new SignupRequest("john@test.com", "@Password123");
        this.authController.registerUser(signupRequest);
        entityManager.flush();

        testUser = userRepository.findByEmail("john@test.com").orElse(null);

        ProfileController.PostProfile postProfile = new ProfileController.PostProfile(
                testUser.getId(),
                "John",
                "Doe",
                "johndoe",
                "123456789",
                "johnGitHub",
                "I am a developer",
                "ROLE_STUDENT",
                "Learning Java",
                testCohortId,
                "1990-01-01",
                "2030-01-01",
                "https://example.com/john.jpg"
        );
        this.profileController.createProfile(postProfile);
        entityManager.flush();

        SignupRequest signupRequest2 = new SignupRequest("jane@test.com", "@Password123");
        this.authController.registerUser(signupRequest2);
        entityManager.flush();

        testUser2 = userRepository.findByEmail("jane@test.com").orElse(null);

        ProfileController.PostProfile postProfile2 = new ProfileController.PostProfile(
                testUser2.getId(),
                "Jane",
                "Smith",
                "janesmith",
                "987654321",
                "janeGitHub",
                "I am also a developer",
                "ROLE_STUDENT",
                "Learning Java too",
                testCohortId,
                "1992-01-01",
                "2030-01-01",
                "https://example.com/jane.jpg"
        );
        this.profileController.createProfile(postProfile2);
        entityManager.flush();

        Post testPost = new Post("This is a test post", testUser, 0);
        testPost = postRepository.save(testPost);
        testPostId = testPost.getId();
        entityManager.flush();

        Comment testComment = new Comment("This is a test comment", testUser2, testPost);
        testComment = commentRepository.save(testComment);
        testCommentId = testComment.getId();
        entityManager.flush();
        entityManager.clear();

        testUser = userRepository.findById(testUser.getId()).orElse(null);
        testUser2 = userRepository.findById(testUser2.getId()).orElse(null);
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
        assertNotNull(webApplicationContext.getBean("postController"));
    }

    @Test
    public void tryGetAllPosts_testPostContentAndAuthor_withSinglePostInDb() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/posts")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
        JSONArray posts = response.getJSONArray("posts");
        assertNotNull(posts);
        assertEquals(1, posts.length());

        JSONObject firstPost = posts.getJSONObject(0);
        assertEquals("This is a test post", firstPost.getString("content"));
        assertEquals("John", firstPost.getJSONObject("user").getJSONObject("profile").getString("firstName"));
        assertEquals("Doe", firstPost.getJSONObject("user").getJSONObject("profile").getString("lastName"));
    }

    @Test
    public void tryCreatePost_testPostCreation_withAuthenticatedUser() throws Exception {
        authenticateUser(testUser);

        String requestBody = """
                {
                    "content": "This is a new test post"
                }
                """;

        MvcResult result = this.mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        assertEquals("This is a new test post", response.getString("content"));
        assertEquals("John", response.getJSONObject("user").getJSONObject("profile").getString("firstName"));
        assertEquals(0, response.getInt("likes"));
    }

    @Test
    public void tryCreatePost_testUnauthorized_withoutAuthentication() throws Exception {
        String requestBody = """
                {
                    "content": "This should fail"
                }
                """;

        this.mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void tryGetPostById_testPostDetails_withValidId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/posts/" + testPostId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        assertEquals("This is a test post", response.getString("content"));
        assertEquals("John", response.getJSONObject("user").getJSONObject("profile").getString("firstName"));
        assertEquals(testPostId, response.getInt("id"));
    }

    @Test
    public void tryGetPostById_testNotFound_withInvalidId() throws Exception {
        this.mockMvc.perform(get("/posts/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void tryDeletePostById_testPostDeletion_withValidId() throws Exception {
        MvcResult result = this.mockMvc.perform(delete("/posts/" + testPostId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);
        assertEquals("This is a test post", response.getString("content"));

        Post deletedPost = postRepository.findById(testPostId).orElse(null);
        assertNull(deletedPost);
    }

    @Test
    public void tryAddCommentToPost_testCommentCreation_withValidData() throws Exception {
        String requestBody = """
                {
                    "body": "This is a new comment",
                    "userId": %d
                }
                """.formatted(testUser2.getId());

        MvcResult result = this.mockMvc.perform(post("/posts/" + testPostId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("comment");
        assertNotNull(response);

        assertEquals("This is a new comment", response.getString("body"));
        assertEquals(testUser2.getId(), response.getJSONObject("user").getInt("id"));
    }

    @Test
    public void tryGetCommentsForPost_testCommentsRetrieval_withValidPostId() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/posts/" + testPostId + "/comments")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        JSONArray comments = response.getJSONArray("comments");
        assertEquals(1, comments.length());
        assertEquals("This is a test comment", comments.getJSONObject(0).getString("body"));
    }

    @Test
    public void tryGetCommentById_testCommentRetrieval_withValidIds() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/posts/" + testPostId + "/comments/" + testCommentId)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("comment");
        assertNotNull(response);

        assertEquals("This is a test comment", response.getString("body"));
        assertEquals(testCommentId, response.getInt("id"));
    }

    @Test
    public void tryUpdateComment_testCommentUpdate_withOwnerAuthentication() throws Exception {
        authenticateUser(testUser2);

        String requestBody = """
                {
                    "body": "This is an updated comment"
                }
                """;

        MvcResult result = this.mockMvc.perform(put("/posts/" + testPostId + "/comments/" + testCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("comment");
        assertNotNull(response);

        assertEquals("This is an updated comment", response.getString("body"));
        assertEquals(testCommentId, response.getInt("id"));
    }

    @Test
    public void tryUpdateComment_testForbidden_withNonOwnerAuthentication() throws Exception {
        authenticateUser(testUser);

        String requestBody = """
                {
                    "body": "This should fail"
                }
                """;

        this.mockMvc.perform(put("/posts/" + testPostId + "/comments/" + testCommentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void tryDeleteComment_testCommentDeletion_withOwnerAuthentication() throws Exception {
        authenticateUser(testUser2);

        this.mockMvc.perform(delete("/posts/" + testPostId + "/comments/" + testCommentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Comment deletedComment = commentRepository.findById(testCommentId).orElse(null);
        assertNull(deletedComment);
    }

    @Test
    public void tryLikePost_testPostLiking_withAuthenticatedUser() throws Exception {
        authenticateUser(testUser);

        MvcResult result = this.mockMvc.perform(post("/posts/" + testPostId + "/like"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        assertEquals(1, response.getInt("likes"));
        assertEquals(testPostId, response.getInt("id"));
    }

    @Test
    public void tryUnlikePost_testPostUnliking_withAuthenticatedUser() throws Exception {
        Post post = postRepository.findById(testPostId).orElse(null);
        post.setLikes(1);
        postRepository.save(post);
        entityManager.flush();

        authenticateUser(testUser);

        MvcResult result = this.mockMvc.perform(delete("/posts/" + testPostId + "/like"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        assertEquals(0, response.getInt("likes"));
        assertEquals(testPostId, response.getInt("id"));
    }

    @Test
    public void tryUpdatePost_testPostUpdate_withOwnerAuthentication() throws Exception {
        authenticateUser(testUser);

        String requestBody = """
                {
                    "content": "This is an updated post content"
                }
                """;

        MvcResult result = this.mockMvc.perform(put("/posts/" + testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        JSONObject response = new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONObject("post");
        assertNotNull(response);

        assertEquals("This is an updated post content", response.getString("content"));
        assertEquals(testPostId, response.getInt("id"));
        assertNotNull(response.getString("timeUpdated"));
    }

    @Test
    public void tryUpdatePost_testForbidden_withNonOwnerAuthentication() throws Exception {
        authenticateUser(testUser2);

        String requestBody = """
                {
                    "content": "This should fail"
                }
                """;

        this.mockMvc.perform(put("/posts/" + testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    public void tryUpdatePost_testBadRequest_withEmptyContent() throws Exception {
        authenticateUser(testUser);

        String requestBody = """
                {
                    "content": ""
                }
                """;

        this.mockMvc.perform(put("/posts/" + testPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}