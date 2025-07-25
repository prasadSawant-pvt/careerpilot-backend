package com.pathprep.controller;

import com.pathprep.dto.DetailedRoadmapRequest;
import com.pathprep.dto.ApiResponse;
import com.pathprep.dto.response.DetailedRoadmapResponse;
import com.pathprep.service.DetailedRoadmapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing detailed learning roadmaps.
 * Provides endpoints for generating and retrieving comprehensive learning paths.
 */
@RestController
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Detailed Roadmap", description = "API for generating and managing detailed learning roadmaps")
public class DetailedRoadmapController {

    private final DetailedRoadmapService roadmapService;

    @Operation(
        summary = "Generate or retrieve a detailed learning roadmap",
        description = "Generates a new detailed learning roadmap or retrieves an existing one based on role and experience level."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully generated or retrieved roadmap",
            content = @Content(schema = @Schema(implementation = DetailedRoadmapResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid input parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping("/detailed")
    public Mono<ResponseEntity<ApiResponse<DetailedRoadmapResponse>>> generateOrGetRoadmap(
            @Valid @RequestBody DetailedRoadmapRequest request) {
        
        log.info("Received request to generate/retrieve roadmap for role: {}, level: {}", 
                request.getRole(), request.getExperienceLevel());
        
        return roadmapService.generateOrGetRoadmap(request)
                .map(roadmap -> ResponseEntity.ok(
                        ApiResponse.<DetailedRoadmapResponse>builder()
                                .success(true)
                                .data(roadmap)
                                .message("Roadmap generated successfully")
                                .statusCode(200)
                                .build()
                ))
                .onErrorResume(e -> {
                    log.error("Error generating roadmap: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.<DetailedRoadmapResponse>builder()
                                    .success(false)
                                    .message("Failed to generate roadmap: " + e.getMessage())
                                    .statusCode(500)
                                    .build()
                            ));
                });
    }

    @Operation(
        summary = "Get a detailed roadmap by composite key",
        description = "Retrieves a detailed learning roadmap by its composite key (format: role_experienceLevel)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved roadmap",
            content = @Content(schema = @Schema(implementation = DetailedRoadmapResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Roadmap not found"
        )
    })
    @GetMapping("/detailed/{compositeKey}")
    public Mono<ResponseEntity<ApiResponse<DetailedRoadmapResponse>>> getRoadmapByCompositeKey(
            @PathVariable String compositeKey) {
        
        log.debug("Fetching roadmap with key: {}", compositeKey);
        
        return roadmapService.getRoadmapByCompositeKey(compositeKey)
                .map(roadmap -> ResponseEntity.ok(
                        ApiResponse.<DetailedRoadmapResponse>builder()
                                .success(true)
                                .data(roadmap)
                                .message("Roadmap retrieved successfully")
                                .statusCode(200)
                                .build()
                ))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<DetailedRoadmapResponse>builder()
                                .success(false)
                                .message("Roadmap not found with key: " + compositeKey)
                                .statusCode(404)
                                .build()
                        )));
    }

    @Operation(
        summary = "Delete a roadmap by ID",
        description = "Deletes a detailed learning roadmap by its ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Roadmap deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Roadmap not found"
        )
    })
    @DeleteMapping("/detailed/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteRoadmap(@PathVariable String id) {
        log.info("Deleting roadmap with ID: {}", id);
        
        return roadmapService.deleteRoadmap(id)
                .then(Mono.just(ResponseEntity.ok(
                        ApiResponse.<Void>builder()
                                .success(true)
                                .message("Roadmap deleted successfully")
                                .statusCode(200)
                                .build()
                )))
                .onErrorResume(e -> {
                    log.error("Error deleting roadmap: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(ApiResponse.<Void>builder()
                                    .success(false)
                                    .message("Failed to delete roadmap: " + e.getMessage())
                                    .statusCode(500)
                                    .build()
                            ));
                });
    }
}
