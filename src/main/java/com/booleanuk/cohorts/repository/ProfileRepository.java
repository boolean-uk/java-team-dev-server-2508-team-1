package com.booleanuk.cohorts.repository;

import com.booleanuk.cohorts.models.Profile;
import com.booleanuk.cohorts.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    List<Profile> getProfilesByFirstNameContains(String firstName);

    List<Profile> getProfilesByLastNameContains(String lastName);

    List<Profile> findTop10ByOrderByIdDesc();

    List<Profile> getProfilesByFirstNameContainingIgnoreCase(String firstName);

    List<Profile> getProfilesByLastNameContainingIgnoreCase(String lastName);

}
