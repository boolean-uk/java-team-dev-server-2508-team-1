package com.booleanuk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.booleanuk.cohorts.models.Cohort;
import com.booleanuk.cohorts.models.Course;
import com.booleanuk.cohorts.models.ERole;
import com.booleanuk.cohorts.models.Post;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.repository.CohortRepository;
import com.booleanuk.cohorts.repository.CourseRepository;
import com.booleanuk.cohorts.repository.PostRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.RoleRepository;
import com.booleanuk.cohorts.repository.UserRepository;

@SpringBootApplication
public class Main implements CommandLineRunner {
    
    // DATABASE SIZE CONFIGURATION
    // Set to true for LARGE database: 3 courses, 4 cohorts per course (12 total), 30 students per cohort (360 total)
    // Set to false for SMALL database: 3 courses, 1 cohort per course (3 total), 10 students per cohort (30 total)
    private static final boolean USE_LARGE_DATABASE = false;
    
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CohortRepository cohortRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    PasswordEncoder encoder;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Initializing sample data...");
        
        // Create roles
        
        Role teacherRole = createOrGetRole(ERole.ROLE_TEACHER);
        Role studentRole = createOrGetRole(ERole.ROLE_STUDENT);
        
        // Create courses
        Course javaFundamentals = createOrGetCourse("Software Development");
        Course springBoot = createOrGetCourse("Front-End Development");
        Course reactFundamentals = createOrGetCourse("Data Analytics");
        
        // Create cohorts based on configuration
        List<Cohort> allCohorts = new ArrayList<>();
        
        if (USE_LARGE_DATABASE) {
            // LARGE DATABASE: 4 cohorts per course (12 total)
            
            // Software Development cohorts (4 cohorts)
            allCohorts.add(createOrGetCohort("Software Development 2024 Q1", javaFundamentals));
            allCohorts.add(createOrGetCohort("Software Development 2024 Q2", javaFundamentals));
            allCohorts.add(createOrGetCohort("Software Development 2025 Q1", javaFundamentals));
            allCohorts.add(createOrGetCohort("Software Development 2025 Q2", javaFundamentals));

            // Front-End Development cohorts (4 cohorts)
            allCohorts.add(createOrGetCohort("Front-End Development 2024 Q1", springBoot));
            allCohorts.add(createOrGetCohort("Front-End Development 2024 Q2", springBoot));
            allCohorts.add(createOrGetCohort("Front-End Development 2025 Q1", springBoot));
            allCohorts.add(createOrGetCohort("Front-End Development 2025 Q2", springBoot));

            // Data Analytics cohorts (4 cohorts)
            allCohorts.add(createOrGetCohort("Data Analytics 2024 Q1", reactFundamentals));
            allCohorts.add(createOrGetCohort("Data Analytics 2024 Q2", reactFundamentals));
            allCohorts.add(createOrGetCohort("Data Analytics 2025 Q1", reactFundamentals));
            allCohorts.add(createOrGetCohort("Data Analytics 2025 Q2", reactFundamentals));
        } else {
            // SMALL DATABASE: 1 cohort per course (3 total)
            allCohorts.add(createOrGetCohort("Software Development 2025", javaFundamentals));
            allCohorts.add(createOrGetCohort("Front-End Development 2025", springBoot));
            allCohorts.add(createOrGetCohort("Data Analytics 2025", reactFundamentals));
        }

        
        // Create teacher users
        User teacherJohn = createUser("t@t.com", "p", teacherRole);
        if (teacherJohn.getProfile() == null) {
            Profile johnProfile = new Profile(teacherJohn, "John", "Smith", "johnsmith", 
                "https://github.com/johnsmith", "+44123456790", 
                "Experienced Java developer and educator with 10+ years in software development.", 
                teacherRole, "Java Development", allCohorts.get(0), null);
            profileRepository.save(johnProfile);
            teacherJohn.setProfile(johnProfile);
            userRepository.save(teacherJohn);
        }
        
        User teacherSarah = createUser("tt@t.com", "p", teacherRole);
        if (teacherSarah.getProfile() == null) {
            Profile sarahProfile = new Profile(teacherSarah, "Sarah", "Jones", "sarahjones", 
                "https://github.com/sarahjones", "+44123456791", 
                "Frontend specialist with expertise in React and modern web technologies.", 
                teacherRole, "Frontend Development", allCohorts.get(1), null);
            profileRepository.save(sarahProfile);
            teacherSarah.setProfile(sarahProfile);
            userRepository.save(teacherSarah);
        }
        
        // Create student users
        List<User> students = new ArrayList<>();
        String[] firstNames = {
            "Alice", "Bob", "Carol", "David", "Emma", "Frank", "Grace", "Henry",
            "Ivy", "Jack", "Kate", "Liam", "Maya", "Noah", "Olivia", "Paul",
            "Quinn", "Ruby", "Sam", "Tina", "Uma", "Victor", "Wendy", "Xavier",
            "Yara", "Zoe", "Alex", "Blake", "Chloe", "Dan", "Eva", "Felix",
            "Gina", "Hugo", "Iris", "Jake", "Luna", "Max", "Nina", "Oscar"
        };
        String[] lastNames = {
            "Johnson", "Wilson", "Brown", "Taylor", "Davis", "Miller", "Anderson", "Thomas",
            "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson",
            "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young",
            "Hernandez", "King", "Wright", "Lopez", "Hill", "Scott", "Green", "Adams",
            "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner"
        };
        
        // Create students based on configuration
        int totalStudents = USE_LARGE_DATABASE ? 360 : 30;  // 360 for large (30 per cohort), 30 for small (10 per cohort)
        int studentsPerCohort = USE_LARGE_DATABASE ? 30 : 10;
        
        for (int i = 0; i < totalStudents; i++) {
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];
            String email = firstName.toLowerCase() + i + "@s.com";

            User student = createUser(email, "p", studentRole);
            if (student.getProfile() == null) {
                // Distribute students evenly across all cohorts
                Cohort assignedCohort = allCohorts.get(i / studentsPerCohort);
                
                Profile studentProfile = new Profile(student, firstName, lastName, 
                    firstName.toLowerCase() + lastName.toLowerCase() + i, 
                    "https://github.com/" + firstName.toLowerCase() + lastName.toLowerCase() + i, 
                    "+4412345679" + String.format("%03d", i), 
                    "Passionate about learning software development and building amazing applications.", 
                    studentRole, "Software Development", assignedCohort, null);
                profileRepository.save(studentProfile);
                student.setProfile(studentProfile);
                student = userRepository.save(student);
            }
            students.add(student);
        }
        
        // Create sample posts
        List<String> samplePosts = Arrays.asList(
            "Just finished my first Java application! Excited to learn more about Spring Boot.",
            "Working on a React project today. The component lifecycle is fascinating!",
            "Had a great debugging session today. Finally understood how to use breakpoints effectively.",
            "Group project is going well. Collaboration through Git is becoming second nature.",
            "Completed the database design exercise. Understanding relationships is key!",
            "Learned about REST APIs today. Can't wait to build my own!",
            "Struggling with CSS Grid but making progress. Practice makes perfect!",
            "Just deployed my first application to the cloud. Such a great feeling!",
            "Code review session was very helpful. Learning from others is invaluable.",
            "Working on the final project. Bringing everything together is challenging but rewarding."
        );
        
        // Create posts from different users (only if no posts exist)
        if (postRepository.count() == 0) {
            for (int i = 0; i < samplePosts.size(); i++) {
                User author = (i < 2) ? (i == 0 ? teacherJohn : teacherSarah) : students.get(i % students.size());
                Post post = new Post(author, samplePosts.get(i));
                post.setLikes((int) (Math.random() * 10)); // Random likes 0-9
                postRepository.save(post);
            }
        }
        
        System.out.println("Sample data initialization completed!");
        System.out.println("Created:");
        System.out.println("- 2 roles (Teacher, Student)");
        System.out.println("- 3 courses (Software Development, Front-End Development, Data Analytics)");
        
        if (USE_LARGE_DATABASE) {
            System.out.println("- 12 cohorts (4 per course)");
            System.out.println("- " + (2 + students.size()) + " users with profiles (2 teachers + " + students.size() + " students)");
            System.out.println("- 30 students per cohort across 12 cohorts (360 total students)");
            System.out.println("  - Software Development: 4 cohorts (120 students)");
            System.out.println("  - Front-End Development: 4 cohorts (120 students)");
            System.out.println("  - Data Analytics: 4 cohorts (120 students)");
        } else {
            System.out.println("- 3 cohorts (1 per course)");
            System.out.println("- " + (2 + students.size()) + " users with profiles (2 teachers + " + students.size() + " students)");
            System.out.println("- 10 students per cohort across 3 cohorts (30 total students)");
            System.out.println("  - Software Development: 1 cohort (10 students)");
            System.out.println("  - Front-End Development: 1 cohort (10 students)");
            System.out.println("  - Data Analytics: 1 cohort (10 students)");
        }
        
        System.out.println("- " + samplePosts.size() + " sample posts");
    }
    
    private Role createOrGetRole(ERole roleName) {
        return roleRepository.findByName(roleName)
            .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
    
    private User createUser(String email, String password, Role role) {
        // Check if user already exists
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                User user = new User(email, encoder.encode(password));
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
                return userRepository.save(user);
            });
    }
    
    private Course createOrGetCourse(String name) {
        return courseRepository.findAll().stream()
            .filter(course -> course.getName().equals(name))
            .findFirst()
            .orElseGet(() -> courseRepository.save(new Course(name)));
    }
    
    private Cohort createOrGetCohort(String name, Course course) {
        return cohortRepository.findAll().stream()
            .filter(cohort -> cohort.getName().equals(name))
            .findFirst()
            .orElseGet(() -> {
                Cohort cohort = new Cohort(name, course);
                return cohortRepository.save(cohort);
            });
    }
}
