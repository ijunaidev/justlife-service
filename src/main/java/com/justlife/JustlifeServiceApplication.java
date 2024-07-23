package com.justlife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class JustlifeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JustlifeServiceApplication.class, args);
    }

}
