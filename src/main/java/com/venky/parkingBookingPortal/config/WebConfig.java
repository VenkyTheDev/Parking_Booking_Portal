package com.venky.parkingBookingPortal.config;

import com.venky.parkingBookingPortal.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private JwtInterceptor jwtInterceptor;

    @Autowired
    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the JWT interceptor and exclude public endpoints
        registry.addInterceptor(jwtInterceptor)
                .excludePathPatterns(
                        "/api/auth/login",          // Exclude login (no need for JWT)
                        "/api/auth/signup",         // Exclude signup (no need for JWT)
                        "/api/auth/logout",         // Exclude logout (no need for JWT)
                        "/api/organisations/**"   // Exclude public org routes
                        )         // Exclude public parking routes
                .addPathPatterns("/api/**"); // Apply interceptor to all other API routes
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // Apply to all endpoints
                .allowedOrigins("http://localhost:5173")  // Replace with your frontend's origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allowed HTTP methods
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers")  // Allowed headers
                .allowCredentials(true)  // Allow cookies and authorization headers
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}
