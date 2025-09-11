package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.response.ErrorResponse;
import com.booleanuk.cohorts.payload.response.Response;
import com.booleanuk.cohorts.payload.response.UserListResponse;
import com.booleanuk.cohorts.payload.response.UserResponse;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static java.util.Arrays.stream;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        UserListResponse userListResponse = new UserListResponse();
        userListResponse.set(this.userRepository.findAll());
        return ResponseEntity.ok(userListResponse);
    }

    @GetMapping("{id}")
    public ResponseEntity<Response> getUserById(@PathVariable int id) {
        User user = this.userRepository.findById(id).orElse(null);
        if (user == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        UserResponse userResponse = new UserResponse();
        userResponse.set(user);
        return ResponseEntity.ok(userResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<?> updateUserWithProfile(@PathVariable int id) {
        int profileId = profileRepository.findAll().stream().filter(it -> it.getUser().getId() == id).toList().getFirst().getId();
        Profile profile = profileRepository.findById(profileId).orElse(null);
        if (profile == null){
            return new ResponseEntity<>("Could not add profile to user because the profile does not exist", HttpStatus.NOT_FOUND);
        }
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
        if (user.getProfile() != null) {
            return new ResponseEntity<>("A profile is already registered on this user", HttpStatus.BAD_REQUEST);
        }
        user.setProfile(profile);
        try {
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("User has an existing profile", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public void registerUser() {
        System.out.println("Register endpoint hit");
    }
}
