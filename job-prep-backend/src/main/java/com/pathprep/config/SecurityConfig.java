package com.pathprep.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Configure request authorization
            .authorizeHttpRequests(auth -> auth
                // Allow all Swagger UI resources
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/api-docs/**"
                ).permitAll()
                
                // Allow all API endpoints (you can secure these later as needed)
                .requestMatchers(
                    "/api/**",
                    "/roadmaps/**",
                    "/roadmap/**",  // Added to match the endpoint with path variables
                    "/roles/**",
                    "/skills/**",
                    "/skill-resources/**",
                    "/interview-questions/**"
                ).permitAll()
                
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
            
            // Disable form login and basic auth for now
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
            
        return http.build();
    }
}
