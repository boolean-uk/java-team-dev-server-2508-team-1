package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.payload.response.ProfileListResponse;
import com.booleanuk.cohorts.payload.response.UserListResponse;
import com.booleanuk.cohorts.repository.ProfileRepository;
import com.booleanuk.cohorts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @GetMapping("/profiles/{query}")
    public ResponseEntity<?> searchProfiles(@PathVariable String query) {
        List<Profile> result = new LinkedList<Profile>();
        profileRepository.getProfilesByFirstNameContainingIgnoreCase(query).forEach(result::add);
        profileRepository.getProfilesByLastNameContainingIgnoreCase(query).forEach(result::add);


        ProfileListResponse profileListResponse = new ProfileListResponse();
        profileListResponse.set(result);

        return new ResponseEntity<>(profileListResponse, HttpStatus.OK);


    }

    @GetMapping("/profiles")
    public ResponseEntity<ProfileListResponse> searchProfilesDefault() {
        List<Profile> result = new LinkedList<Profile>();
        profileRepository.findTop10ByOrderByIdDesc().forEach(result::add);

        ProfileListResponse profileListResponse = new ProfileListResponse();
        profileListResponse.set(result);
        return new ResponseEntity<>(profileListResponse,HttpStatus.OK);
    }
}
