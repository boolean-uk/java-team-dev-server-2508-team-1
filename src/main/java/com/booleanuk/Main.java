package com.booleanuk;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class Main implements CommandLineRunner {
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
    PasswordEncoder encoder;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        Role teacherRole;
        if (!this.roleRepository.existsByName(ERole.ROLE_TEACHER)) {
            teacherRole = this.roleRepository.save(new Role(ERole.ROLE_TEACHER));
        } else {
            teacherRole = this.roleRepository.findByName(ERole.ROLE_TEACHER).orElse(null);
        }
        Set<Role> teacherRoles = new HashSet<>();
        teacherRoles.add(teacherRole);
        Role studentRole;
        if (!this.roleRepository.existsByName(ERole.ROLE_STUDENT)) {
            studentRole = this.roleRepository.save(new Role(ERole.ROLE_STUDENT));
        } else {
            studentRole = this.roleRepository.findByName(ERole.ROLE_STUDENT).orElse(null);
        }
        Set<Role> studentRoles = new HashSet<>();
        studentRoles.add(studentRole);
        if (!this.roleRepository.existsByName(ERole.ROLE_ADMIN)) {
            this.roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
        // Create a cohort.
        Cohort cohort;
        if (!this.cohortRepository.existsById(1)) {
             cohort = this.cohortRepository.save(new Cohort());
        } else {
            cohort = this.cohortRepository.findById(1).orElse(null);
        }
        // Create some users
        User studentUser;
        if (!this.userRepository.existsById(1)) {
            studentUser = new User("student@test.com", this.encoder.encode("Testpassword1!"), cohort);
            studentUser.setRoles(studentRoles);
            studentUser = this.userRepository.save(studentUser);
        } else {
            studentUser = this.userRepository.findById(1).orElse(null);
        }
        Profile studentProfile;
        if (!this.profileRepository.existsById(1)) {
            studentProfile = this.profileRepository.save(new Profile(studentUser, "Joe", "Bloggs", "Hello world!", "student1", "11111111"));
        } else {
            studentProfile = this.profileRepository.findById(1).orElse(null);
        }

        User teacherUser;
        if (!this.userRepository.existsById(2)) {
            teacherUser = new User("dave@email.com", this.encoder.encode("password"));
            teacherUser.setRoles(teacherRoles);
            teacherUser = this.userRepository.save(teacherUser);
        } else {
            teacherUser = this.userRepository.findById(2).orElse(null);
        }
        Profile teacherProfile;
        if (!this.profileRepository.existsById(2)) {
            teacherProfile = this.profileRepository.save(new Profile(teacherUser, "Rick", "Sanchez", "Hello there!", "teacher1", "88888888"));
        } else {
            teacherProfile = this.profileRepository.findById(2).orElse(null);
        }

        if (!this.postRepository.existsById(1)) {
            this.postRepository.save(new Post(studentUser, "My first post!"));
        }
        if (!this.postRepository.existsById(2)) {
            this.postRepository.save(new Post(teacherUser, "Hello, students!"));
        }
    }
}
