//package com.venky.parkingBookingPortal.security;
//
//
//import com.venky.parkingBookingPortal.security.JwtAuthenticationFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableMethodSecurity // Enables @PreAuthorize annotations
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthFilter;
//    private final UserDetailsService userDetailsService;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
//        this.jwtAuthFilter = jwtAuthFilter;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return new ProviderManager(authProvider);
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())  // Disable CSRF for JWT-based authentication
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/login", "/auth/signup").permitAll()  // Allow login/signup
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Only admins can access /api/admin
//                        .anyRequest().authenticated()  // All other routes require authentication
//                )
//                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))  // Stateless JWT authentication
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);  // Add JWT filter before authentication
//
//        return http.build();
//    }
//}
