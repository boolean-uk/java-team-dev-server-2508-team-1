package com.booleanuk.cohorts.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booleanuk.cohorts.models.Post;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.response.ErrorResponse;
import com.booleanuk.cohorts.payload.response.Response;
import com.booleanuk.cohorts.payload.response.UserListResponse;
import com.booleanuk.cohorts.payload.response.UserResponse;
import com.booleanuk.cohorts.repository.PostRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private PostRepository postRepository;

    @GetMapping
    public ResponseEntity<UserListResponse> getAllUsers() {
        UserListResponse userListResponse = new UserListResponse();
        userListResponse.set(this.userRepository.findAll());
        return ResponseEntity.ok(userListResponse);
    }

    record PostId(
            int post_id
    ){}

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

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        User user = this.userRepository.findById(id).orElse(null);
        if (user == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        for (Post likedPost : new HashSet<>(user.getLikedPosts())) {
            likedPost.getLikedByUsers().remove(user);
        }
        user.getLikedPosts().clear();

        UserResponse userResponse = new UserResponse();
        userResponse.set(user);

        try {
            userRepository.delete(user);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return new ResponseEntity<>("Could not delete user: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("{user_id}/like")
    public ResponseEntity<Response> updateLikedPosts(@PathVariable int user_id, @RequestBody PostId postId){
        int post_id = postId.post_id;
        User user = userRepository.findById(user_id).orElse(null);
        Post post = postRepository.findById(post_id).orElse(null);
        ErrorResponse errorResponse = new ErrorResponse();

        if (user == null) {
            errorResponse.set("User not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        if (post == null) {
            errorResponse.set("Post not found");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
        Set<Post> likedPosts = user.getLikedPosts();

        if (likedPosts.contains(post)) {
            likedPosts.remove(post);
        } else {
            likedPosts.add(post);
        }
        user.setLikedPosts(likedPosts);
        userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.set(user);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    public void registerUser() {
        System.out.println("Register endpoint hit");
    }
}
