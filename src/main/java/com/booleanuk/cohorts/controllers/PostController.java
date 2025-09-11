package com.booleanuk.cohorts.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booleanuk.cohorts.models.Author;
import com.booleanuk.cohorts.models.Comment;
import com.booleanuk.cohorts.models.Post;
import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.CommentRequest;
import com.booleanuk.cohorts.payload.response.CommentResponse;
import com.booleanuk.cohorts.payload.response.ErrorResponse;
import com.booleanuk.cohorts.payload.response.PostListResponse;
import com.booleanuk.cohorts.payload.response.PostResponse;
import com.booleanuk.cohorts.payload.response.Response;
import com.booleanuk.cohorts.repository.CommentRepository;
import com.booleanuk.cohorts.repository.PostRepository;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import com.booleanuk.cohorts.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("posts")
public class PostController {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private CommentRepository commentRepository;

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return this.userRepository.findByEmail(userDetails.getEmail()).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        PostListResponse postListResponse = new PostListResponse();
        
        List<Post> posts = this.postRepository.findAll();
        
        // For each post, set up the author information
        for (Post post : posts) {
            User user = post.getUser();
            if (user != null) {
                Profile profile = this.profileRepository.findById(user.getId()).orElse(null);
                if (profile != null && user.getCohort() != null) {
                    Author author = new Author(
                        user.getId(), 
                        user.getCohort().getId(), 
                        profile.getFirstName(),
                        profile.getLastName(), 
                        user.getEmail(), 
                        profile.getBio(), 
                        profile.getGithubUrl()
                    );
                    post.setAuthor(author);
                }
            }
        }
        
        postListResponse.set(posts);
        return ResponseEntity.ok(postListResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPostById(@PathVariable int id) {
        Post post = this.postRepository.findById(id).orElse(null);
        
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        
        // Set up the author information
        User user = post.getUser();
        if (user != null) {
            Profile profile = this.profileRepository.findById(user.getId()).orElse(null);
            if (profile != null && user.getCohort() != null) {
                Author author = new Author(
                    user.getId(), 
                    user.getCohort().getId(), 
                    profile.getFirstName(),
                    profile.getLastName(), 
                    user.getEmail(), 
                    profile.getBio(), 
                    profile.getGithubUrl()
                );
                post.setAuthor(author);
            }
        }
        
        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Response> addCommentToPost(@PathVariable int postId, @RequestBody CommentRequest commentRequest) {
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        User user = this.userRepository.findById(commentRequest.getUserId()).orElse(null);
        if (user == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("User not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        Comment comment = new Comment(commentRequest.getBody(), user, post);
        Comment savedComment = this.commentRepository.save(comment);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(savedComment);
        return new ResponseEntity<>(commentResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Response> getCommentsForPost(@PathVariable int postId) {
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Return the post with its comments (comments will be included via the @OneToMany relationship)
        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> getCommentById(@PathVariable int postId, @PathVariable int commentId) {
        // Verify post exists
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the comment
        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Verify the comment belongs to the specified post
        if (comment.getPost().getId() != postId) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment does not belong to the specified post");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(comment);
        return ResponseEntity.ok(commentResponse);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> updateComment(@PathVariable int postId, @PathVariable int commentId, @RequestBody CommentRequest commentRequest) {
        // Get the current authenticated user
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Authentication required");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        // Verify post exists
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the comment
        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Verify the comment belongs to the specified post
        if (comment.getPost().getId() != postId) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment does not belong to the specified post");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        // Verify the user is the owner of the comment
        if (comment.getUser().getId() != currentUser.getId()) {
            ErrorResponse error = new ErrorResponse();
            error.set("You can only edit your own comments");
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        // Update the comment
        comment.setBody(commentRequest.getBody());
        Comment updatedComment = this.commentRepository.save(comment);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(updatedComment);
        return ResponseEntity.ok(commentResponse);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> deleteComment(@PathVariable int postId, @PathVariable int commentId) {
        // Get the current authenticated user
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Authentication required");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        // Verify post exists
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Post not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Get the comment
        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        // Verify the comment belongs to the specified post
        if (comment.getPost().getId() != postId) {
            ErrorResponse error = new ErrorResponse();
            error.set("Comment does not belong to the specified post");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        // Verify the user is the owner of the comment
        if (comment.getUser().getId() != currentUser.getId()) {
            ErrorResponse error = new ErrorResponse();
            error.set("You can only delete your own comments");
            return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
        }

        // Delete the comment
        this.commentRepository.delete(comment);

        // Return success message
        ErrorResponse success = new ErrorResponse();
        success.set("Comment deleted successfully");
        return ResponseEntity.ok(success);
    }
}
