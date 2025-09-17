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
import com.booleanuk.cohorts.payload.request.PostRequest;
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

    private ResponseEntity<Response> unauthorizedResponse() {
        ErrorResponse error = new ErrorResponse();
        error.set("Authentication required");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<Response> notFoundResponse(String message) {
        ErrorResponse error = new ErrorResponse();
        error.set(message);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Response> badRequestResponse(String message) {
        ErrorResponse error = new ErrorResponse();
        error.set(message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Response> forbiddenResponse(String message) {
        ErrorResponse error = new ErrorResponse();
        error.set(message);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    private void setAuthorInfo(Post post) {
        User user = post.getUser();
        if (user != null && user.getCohort() != null) {
            Profile profile = user.getProfile();

            if (profile != null) {
                Author author = new Author(user.getId(), user.getCohort().getId(), 
                    profile.getFirstName(), profile.getLastName(), user.getEmail(), 
                    profile.getBio(), profile.getGithubUrl());
                post.setAuthor(author);
            }
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        List<Post> posts = this.postRepository.findAll();
        posts.forEach(this::setAuthorInfo);
        
        PostListResponse postListResponse = new PostListResponse();
        postListResponse.set(posts);
        return ResponseEntity.ok(postListResponse);
    }

    @PostMapping
    public ResponseEntity<Response> createPost(@RequestBody PostRequest postRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = new Post(postRequest.getContent(), currentUser, 0);
        Post savedPost = this.postRepository.save(post);
        setAuthorInfo(savedPost);

        PostResponse postResponse = new PostResponse();
        postResponse.set(savedPost);
        return new ResponseEntity<>(postResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPostById(@PathVariable int id) {
        Post post = this.postRepository.findById(id).orElse(null);
        if (post == null) return notFoundResponse("Post not found");
        
        setAuthorInfo(post);
        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePostById(@PathVariable int id) {
        Post post = this.postRepository.findById(id).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        postRepository.delete(post);
        return ResponseEntity.ok(postResponse);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Response> addCommentToPost(@PathVariable int postId, @RequestBody CommentRequest commentRequest) {
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        User user = this.userRepository.findById(commentRequest.getUserId()).orElse(null);
        if (user == null) return notFoundResponse("User not found");

        Comment comment = new Comment(commentRequest.getBody(), user, post);
        Comment savedComment = this.commentRepository.save(comment);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(savedComment);
        return new ResponseEntity<>(commentResponse, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<Response> getCommentsForPost(@PathVariable int postId) {
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        PostResponse postResponse = new PostResponse();
        postResponse.set(post);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> getCommentById(@PathVariable int postId, @PathVariable int commentId) {
        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) return notFoundResponse("Comment not found");

        if (comment.getPost().getId() != postId) 
            return badRequestResponse("Comment does not belong to the specified post");

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(comment);
        return ResponseEntity.ok(commentResponse);
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> updateComment(@PathVariable int postId, @PathVariable int commentId, @RequestBody CommentRequest commentRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) return notFoundResponse("Comment not found");

        if (comment.getPost().getId() != postId) 
            return badRequestResponse("Comment does not belong to the specified post");

        if (comment.getUser().getId() != currentUser.getId()) 
            return forbiddenResponse("You can only edit your own comments");

        comment.setBody(commentRequest.getBody());
        Comment updatedComment = this.commentRepository.save(comment);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.set(updatedComment);
        return ResponseEntity.ok(commentResponse);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Response> deleteComment(@PathVariable int postId, @PathVariable int commentId) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        Comment comment = this.commentRepository.findById(commentId).orElse(null);
        if (comment == null) return notFoundResponse("Comment not found");

        if (comment.getPost().getId() != postId) 
            return badRequestResponse("Comment does not belong to the specified post");

        if (comment.getUser().getId() != currentUser.getId()) 
            return forbiddenResponse("You can only delete your own comments");

        this.commentRepository.delete(comment);

        ErrorResponse success = new ErrorResponse();
        success.set("Comment deleted successfully");
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Response> likePost(@PathVariable int postId) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        post.setLikes(post.getLikes() + 1);
        Post updatedPost = this.postRepository.save(post);
        setAuthorInfo(updatedPost);

        PostResponse postResponse = new PostResponse();
        postResponse.set(updatedPost);
        return ResponseEntity.ok(postResponse);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Response> unlikePost(@PathVariable int postId) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        post.setLikes(Math.max(0, post.getLikes() - 1));
        Post updatedPost = this.postRepository.save(post);
        setAuthorInfo(updatedPost);

        PostResponse postResponse = new PostResponse();
        postResponse.set(updatedPost);
        return ResponseEntity.ok(postResponse);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<Response> updatePost(@PathVariable int postId, @RequestBody PostRequest postRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        if (currentUser == null) return unauthorizedResponse();

        Post post = this.postRepository.findById(postId).orElse(null);
        if (post == null) return notFoundResponse("Post not found");

        // Only the owner can update their post
        if (post.getUser() == null || post.getUser().getId() != currentUser.getId()) {
            return forbiddenResponse("You can only edit your own posts");
        }

        // Update content only; likes unchanged
        if (postRequest.getContent() == null || postRequest.getContent().trim().isEmpty()) {
            return badRequestResponse("Content cannot be empty");
        }
    post.setContent(postRequest.getContent().trim());
    // Explicitly set timeUpdated only on PUT update of post content
    post.setTimeUpdated(java.time.OffsetDateTime.now());

        Post updatedPost = this.postRepository.save(post);
        setAuthorInfo(updatedPost);

        PostResponse postResponse = new PostResponse();
        postResponse.set(updatedPost);
        return ResponseEntity.ok(postResponse);
    }
}
