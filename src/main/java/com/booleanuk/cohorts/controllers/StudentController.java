
package com.booleanuk.cohorts.controllers;


import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.StudentRequest;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("student")
public class StudentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    PasswordEncoder encoder;

    @PatchMapping("{id}")
    public ResponseEntity<?> updateStudent(@PathVariable int id, @RequestBody StudentRequest studentRequest) {

        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Profile profile = profileRepository.findById(id).orElse(null);
        if (profile == null) {
            return new ResponseEntity<>("Profile not found", HttpStatus.NOT_FOUND);
        }

        if (profile.getRole().equals("ROLE_TEACHER")) {
            return new ResponseEntity<>("Only users with the STUDENT role can be viewed.", HttpStatus.BAD_REQUEST);
        }

        profile.setPhoto(studentRequest.getPhoto());
        profile.setFirstName(studentRequest.getFirst_name());
        profile.setLastName(studentRequest.getLast_name());
        profile.setUsername(studentRequest.getUsername());
        profile.setGithubUrl(studentRequest.getGithub_username());

        user.setEmail(studentRequest.getEmail());
        profile.setMobile(studentRequest.getMobile());
        user.setPassword(encoder.encode(studentRequest.getPassword()));
        profile.setBio(studentRequest.getBio());

        profileRepository.save(profile);

        user.setProfile(profile);
        user.setCohort(profile.getCohort());

        try {
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }
    }

}
