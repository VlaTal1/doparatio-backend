package com.vlatal.gitracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GitrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitrackerApplication.class, args);
    }

}
