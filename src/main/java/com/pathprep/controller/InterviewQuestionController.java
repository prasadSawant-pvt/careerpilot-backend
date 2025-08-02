package com.pathprep.controller;

import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
import com.pathprep.dto.SkillQuestionsRequest;
import com.pathprep.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully generated interview questions",
            content = @Content(schema = @Schema(implementation = InterviewQuestionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error generating questions"
        )
    })
    public Mono<ResponseEntity<InterviewQuestionResponse>> generateQuestions(
            @Valid @RequestBody GenerateQuestionsRequest request) {
        
        log.info("Received request to generate interview questions: {}", request);
        return interviewQuestionService.generateQuestions(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error generating interview questions", e);
                    return Mono.just(ResponseEntity
                            .internalServerError()
                            .body(new InterviewQuestionResponse()));
                });
    }

    @GetMapping
    @Operation(summary = "Get interview questions", 
               description = "Retrieves interview questions from the database based on role and experience level")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved interview questions",
            content = @Content(schema = @Schema(implementation = InterviewQuestionResponse.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No questions found for the given criteria"
        )
    })
    public Mono<ResponseEntity<InterviewQuestionResponse>> getQuestions(
            @Parameter(description = "Job role", example = "Java Developer") @RequestParam String role,
            @Parameter(description = "Experience level", example = "Mid") @RequestParam String experienceLevel,
            @Parameter(description = "Number of questions to return", example = "10") 
            @RequestParam(defaultValue = "10") int count,
            @Parameter(description = "Force refresh the cache", example = "false")
            @RequestParam(required = false, defaultValue = "false") boolean forceRefresh) {
        
        return interviewQuestionService.getQuestions(role, experienceLevel, count, forceRefresh)
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(ResponseEntity
                    .notFound()
                    .build()))
            .onErrorResume(e -> {
                log.error("Error fetching interview questions", e);
                if (e instanceof com.pathprep.exception.ResourceNotFoundException) {
                    return Mono.just(ResponseEntity
                            .notFound()
                            .build());
                }
                return Mono.just(ResponseEntity
                        .internalServerError()
                        .build());
            });
    }
    
    @PostMapping("/skill")
    @Operation(summary = "Generate skill-specific interview questions", 
               description = "Generates interview questions focused on a specific skill, with optional experience level and question count")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully generated interview questions",
            content = @Content(schema = @Schema(implementation = InterviewQuestionResponse.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters"
        )
    })
    public Mono<ResponseEntity<InterviewQuestionResponse>> generateSkillQuestions(
            @Valid @RequestBody SkillQuestionsRequest request) {
        
        log.info("Received request to generate skill questions: {}", request);
        
        return interviewQuestionService.generateSkillQuestions(request)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("Error generating skill questions", e);
                if (e instanceof com.pathprep.exception.AIServiceException) {
                    return Mono.just(ResponseEntity
                            .internalServerError()
                            .build());
                }
                return Mono.just(ResponseEntity
                        .internalServerError()
                        .build());
            });
    }
}
