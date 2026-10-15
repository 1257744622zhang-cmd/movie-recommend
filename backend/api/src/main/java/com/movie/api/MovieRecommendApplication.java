package com.movie.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.movie")
@EntityScan(basePackages = "com.movie")
@EnableJpaRepositories(basePackages = "com.movie")
public class MovieRecommendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieRecommendApplication.class, args);
    }
}
