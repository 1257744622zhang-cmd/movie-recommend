package com.movie.config;

import com.movie.entity.User;
import com.movie.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final DataSource dataSource;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataLoader(UserRepository userRepository, DataSource dataSource) {
        this.userRepository = userRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        // 创建管理员账号
        createAdminUser();
        // 从SQL文件加载电影数据
        loadMovieDataFromSql();
    }

    private void createAdminUser() {
        // 查找或创建管理员用户
        User admin = userRepository.findByUsername("admin").orElse(new User());
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole("ADMIN");
        userRepository.save(admin);

        // 查找或创建测试用户
        User user = userRepository.findByUsername("user").orElse(new User());
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user"));
        user.setRole("USER");
        userRepository.save(user);
    }

    private void loadMovieDataFromSql() {
        try (Connection connection = dataSource.getConnection()) {
            // 执行SQL脚本文件，使用UTF-8编码
            ClassPathResource resource = new ClassPathResource("movie_data.sql");
            EncodedResource encodedResource = new EncodedResource(resource, "UTF-8");
            ScriptUtils.executeSqlScript(connection, encodedResource);
            System.out.println("电影数据从SQL文件加载完成");
        } catch (Exception e) {
            System.err.println("加载电影数据失败: " + e.getMessage());
        }
    }
}
