package com.booleanuk.config;

import com.booleanuk.cohorts.repository.UserRepository;
import com.booleanuk.cohorts.controllers.SearchController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean(name = "searchController")
    public SearchController searchController() {
        return new SearchController();
    }

    @Bean
    public UserRepository userRepository() {
        return this.userRepository;
    }
}