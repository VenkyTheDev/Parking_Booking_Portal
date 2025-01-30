package com.venky.parkingBookingPortal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Only ADMIN can access /admin/** URLs
                        .requestMatchers("/user/**").hasRole("USER")   // Only USER can access /user/** URLs
                        .anyRequest().permitAll() // All other requests are allowed without authentication
                )
                .formLogin(login -> login
                        .loginPage("/login") // Custom login page
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("username")
                .password("{noop}password") // {noop} means no password encoding
                .roles("USER") // Assigns "ROLE_USER"
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin123")
                .roles("ADMIN") // Assigns "ROLE_ADMIN"
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

}
