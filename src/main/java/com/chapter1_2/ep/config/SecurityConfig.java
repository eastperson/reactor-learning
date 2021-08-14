package com.chapter1_2.ep.config;

import com.chapter1_2.ep.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;

// 메소드 수준 보안 활성화
@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfig {

    static final String USER = "USER";
    static final String INVENTORY = "INVENTORY";
    static String role(String auth) {
        return "ROLE_" + auth;
    }

    // 스프링 시큐리티가 리액티브 애플리케이션 안에서 찾아서 사용
    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByName(username)
                .map(user -> User.withDefaultPasswordEncoder()
                    .username(user.getName())
                        .password(user.getPassword())
                        .authorities(user.getRoles().toArray(new String[0]))
                        .build());
    }

    // 테스트를 위해 사용자 생성 및 로딩
    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
            operations.save(new com.chapter1_2.ep.model.User(
                    "ep","password", Arrays.asList(role(USER))));

            operations.save(new com.chapter1_2.ep.model.User(
                    "manager","password", Arrays.asList(role(USER),role(INVENTORY))));
        };
    }

    @Bean
    SecurityWebFilterChain myCustomSecurityPolicy(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
//                    .pathMatchers(HttpMethod.POST,"/").hasRole(INVENTORY)
//                    .pathMatchers(HttpMethod.DELETE,"/**").hasRole(INVENTORY)
                    .anyExchange().authenticated()
                        .and()
                        .httpBasic()
                        .and()
                        .formLogin())
                .csrf().disable().build();
    }
}
