package com.booleanuk.cohorts.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.booleanuk.cohorts.models.ERole;
import com.booleanuk.cohorts.models.Role;
import com.booleanuk.cohorts.models.User;
import com.booleanuk.cohorts.payload.request.LoginRequest;
import com.booleanuk.cohorts.payload.request.SignupRequest;
import com.booleanuk.cohorts.payload.response.JwtResponse;
import com.booleanuk.cohorts.payload.response.MessageResponse;
import com.booleanuk.cohorts.payload.response.RefreshTokenResponse;
import com.booleanuk.cohorts.payload.response.TokenResponse;
import com.booleanuk.cohorts.repository.RoleRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import com.booleanuk.cohorts.security.jwt.JwtUtils;
import com.booleanuk.cohorts.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

//fixed issue with login.
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // If using a salt for password use it here
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream().map((item) -> item.getAuthority())
                .collect(Collectors.toList());

        User user = userRepository.findByEmail(userDetails.getEmail()).orElse(null);

        return ResponseEntity
                .ok(new TokenResponse(new JwtResponse(jwt, user)));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }


        String emailRegex = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";
        String passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[#?!@$%^&-]).{8,}$";

        if(!signupRequest.getEmail().matches(emailRegex))
            return ResponseEntity.badRequest().body(new MessageResponse("Email is incorrectly formatted"));

        if(!signupRequest.getPassword().matches(passwordRegex))
            return  ResponseEntity.badRequest().body(new MessageResponse("Password is incorrectly formatted"));

        // Create a new user add salt here if using one
        User user = new User(signupRequest.getEmail(), encoder.encode(signupRequest.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok((new MessageResponse("User registered successfully")));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            // 1. Extract token from Authorization header
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth == null || !headerAuth.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Authorization header missing or invalid"));
            }
            
            String token = headerAuth.substring(7);
            
            // 2. Validate the token (allowing expired tokens for refresh)
            if (!jwtUtils.validateJwtTokenForRefresh(token)) {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid token"));
            }
            
            // 3. Decode token to get user email (handling expired tokens)
            String userEmail = jwtUtils.getUserNameFromExpiredJwtToken(token);
            
            // 4. Fetch updated user from database
            User user = userRepository.findByEmailWithProfile(userEmail).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }
            
            // 5. Create new authentication object with fresh user data
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            // 6. Generate new token with fresh user data
            String newJwt = jwtUtils.generateJwtToken(authentication);
            
            // 7. Return new token in response
            return ResponseEntity.ok(new RefreshTokenResponse(newJwt, "Token refreshed successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error refreshing token: " + e.getMessage()));
        }
    }
}
