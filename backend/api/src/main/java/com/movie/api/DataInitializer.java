package com.movie.api;

import com.movie.user.entity.User;
import com.movie.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("123456");
        
        User admin = userRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setRole("ADMIN");
            admin.setName("管理员");
        }
        admin.setPassword(encodedPassword);
        userRepository.save(admin);
        System.out.println("Initialized admin user: admin / 123456");
        
        User user = userRepository.findByUsername("user").orElse(null);
        if (user == null) {
            user = new User();
            user.setUsername("user");
            user.setRole("USER");
            user.setName("普通用户");
        }
        user.setPassword(encodedPassword);
        userRepository.save(user);
        System.out.println("Initialized test user: user / 123456");
    }
}
