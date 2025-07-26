package com.pathprep.controller;

import com.pathprep.dto.ApiResponse;
import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
import com.pathprep.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/interview-questions")
@RequiredArgsConstructor
@Tag(name = "Interview Questions", description = "API for generating and retrieving interview questions")
public class InterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @PostMapping
    @Operation(summary = "Generate interview questions", 
               description = "Generates interview questions based on role, experience level, and optional topics")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Successfully generated interview questions",
            content = @Content(schema = @Schema(implementation = InterviewQuestionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Error generating questions"
        )
    })
    public Mono<ResponseEntity<ApiResponse<InterviewQuestionResponse>>> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request) {
        
        log.info("Received request to generate interview questions: {}", request);
        return interviewQuestionService.generateQuestions(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Interview questions generated successfully", response)))
                .onErrorResume(e -> {
                    log.error("Error generating interview questions", e);
                    return Mono.just(ResponseEntity
                            .internalServerError()
                            .body(ApiResponse.error("Failed to generate interview questions: " + e.getMessage())));
                });
    }

    @GetMapping
    @Operation(summary = "Get interview questions", 
               description = "Retrieves interview questions from the database based on role and experience level")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved interview questions",
            content = @Content(schema = @Schema(implementation = InterviewQuestionResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "No questions found for the given criteria"
        )
    })
    public Mono<ResponseEntity<ApiResponse<InterviewQuestionResponse>>> getQuestions(
            @RequestParam String role,
            @RequestParam String experienceLevel,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false, defaultValue = "false") boolean forceRefresh) {
        
        log.info("Fetching {} interview questions for {} ({}), forceRefresh: {}", count, role, experienceLevel, forceRefresh);
        
        GenerateQuestionsRequest request = new GenerateQuestionsRequest();
        request.setRole(role);
        request.setExperienceLevel(experienceLevel);
        request.setCount(count);
        request.setForceRefresh(forceRefresh);
        
        return interviewQuestionService.generateQuestions(request)
                .map(response -> ResponseEntity.ok(ApiResponse.success("Interview questions retrieved successfully", response)))
                .onErrorResume(e -> {
                    log.error("Error fetching interview questions", e);
                    if (e instanceof com.pathprep.exception.ResourceNotFoundException) {
                        return Mono.just(ResponseEntity
                                .status(404)
                                .body(ApiResponse.error(404, e.getMessage())));
                    }
                    return Mono.just(ResponseEntity
                            .internalServerError()
                            .body(ApiResponse.error("Failed to fetch interview questions: " + e.getMessage())));
                });
    }
}
