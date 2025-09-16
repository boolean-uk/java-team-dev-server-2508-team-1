package com.booleanuk;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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

    }
}
