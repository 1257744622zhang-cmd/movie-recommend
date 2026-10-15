package com.movie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class MovieRecommendApplication {

    private static final Logger logger = LoggerFactory.getLogger(MovieRecommendApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Movie Recommendation Application...");
        try {
            SpringApplication.run(MovieRecommendApplication.class, args);
            logger.info("Movie Recommendation Application started successfully!");
        } catch (Exception e) {
            logger.error("Error starting Movie Recommendation Application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
