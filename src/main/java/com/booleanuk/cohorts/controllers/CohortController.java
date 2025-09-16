package com.booleanuk.cohorts.controllers;

import com.booleanuk.cohorts.models.*;
import com.booleanuk.cohorts.payload.response.*;
import com.booleanuk.cohorts.repository.CohortRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("cohorts")
public class CohortController {
    @Autowired
    private CohortRepository cohortRepository;

    @GetMapping
    public ResponseEntity<CohortListResponse> getAllCohorts() {
        CohortListResponse cohortListResponse = new CohortListResponse();
        cohortListResponse.set(this.cohortRepository.findAll());
        return ResponseEntity.ok(cohortListResponse);
    }

    @GetMapping("{id}")
    public ResponseEntity<Response> getCohortById(@PathVariable int id) {
        Cohort cohort = this.cohortRepository.findById(id).orElse(null);
        if (cohort == null || cohort.getProfiles().isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.set("not found");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
        CohortResponse cohortResponse = new CohortResponse();
        cohortResponse.set(cohort);
        return ResponseEntity.ok(cohortResponse);
    }
}
