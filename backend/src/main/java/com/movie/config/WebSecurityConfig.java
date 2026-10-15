package com.movie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // 允许公开访问的接口
            .antMatchers("/auth/**", "/uploads/**").permitAll()
            // 电影相关接口的权限控制：ADMIN可以访问所有方法，USER只能访问GET方法
            .antMatchers(HttpMethod.POST, "/movies").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.PUT, "/movies/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/movies/**").hasAuthority("ADMIN")
            .antMatchers(HttpMethod.GET, "/movies", "/movies/**").permitAll()
            // 用户标签相关接口
            .antMatchers("/user-tags/**").permitAll()
            // 收藏相关接口
            .antMatchers("/collections/**").permitAll()
            // 推荐相关接口
            .antMatchers("/recommend/**").permitAll()
            // 评分相关接口
            .antMatchers("/ratings/**").permitAll()
            // 推荐反馈相关接口
            .antMatchers("/recommend-feedback/**").permitAll()
            // 其他接口需要认证
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
