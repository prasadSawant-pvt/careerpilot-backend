package com.pathprep.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(Collections.singletonList(
                        new Server().url(contextPath).description("Default Server URL")
                ))
                .info(new Info()
                        .title("PathPrep API Documentation")
                        .version("1.0")
                        .description("API documentation for PathPrep Backend Service")
                        .contact(new Contact()
                                .name("PathPrep Support")
                                .email("support@pathprep.com")
                                .url("https://pathprep.com/contact"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-apis")
                .pathsToMatch(
                        "/api/roadmaps/**",
                        "/api/roles/**",
                        "/api/skills/**",
                        
"/api/skill-resources/**",
                        "/api/ai/**",
                        "/api/coding-problems/**",
                        "/api/interview-questions/**"
                )
                .build();
    }
}