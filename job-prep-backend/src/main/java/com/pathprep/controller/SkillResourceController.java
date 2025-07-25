package com.pathprep.controller;

import com.pathprep.dto.SkillResourceRequest;
import com.pathprep.dto.ApiResponse;
import com.pathprep.dto.response.SkillResourceResponse;
import com.pathprep.service.SkillResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/skill-resources")
@Tag(name = "Skill Resources", description = "APIs for managing skill learning resources")
public class SkillResourceController {

    private final SkillResourceService skillResourceService;

    @Operation(
        summary = "Get or generate skill resources",
        description = "Retrieves existing skill resources or generates new ones if they don't exist"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved or generated skill resources",
            content = @Content(schema = @Schema(implementation = SkillResourceResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<SkillResourceResponse>>> getOrGenerateSkillResources(
            @Valid @RequestBody SkillResourceRequest request) {
        log.info("Received request for skill resources: {}/{}/{}", 
            request.getSkillName(), request.getRole(), request.getExperienceLevel());
            
        return skillResourceService.getOrGenerateSkillResources(request)
            .map(response -> ResponseEntity.ok(
                ApiResponse.<SkillResourceResponse>builder()
                    .success(true)
                    .data(response)
                    .message("Successfully retrieved skill resources")
                    .build()
            ))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.<SkillResourceResponse>builder()
                    .success(false)
                    .message("Failed to get or generate skill resources: " + e.getMessage())
                    .build()
                )
            ));
    }

    @Operation(
        summary = "Get skill resources by ID",
        description = "Retrieves skill resources by their unique identifier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved skill resources",
            content = @Content(schema = @Schema(implementation = SkillResourceResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Skill resources not found"
        )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<SkillResourceResponse>>> getSkillResourcesById(
            @Parameter(description = "ID of the skill resources to retrieve")
            @PathVariable String id) {
        log.info("Fetching skill resources with ID: {}", id);
        
        return skillResourceService.getSkillResourcesById(id)
            .map(response -> ResponseEntity.ok(
                ApiResponse.<SkillResourceResponse>builder()
                    .success(true)
                    .data(response)
                    .message("Successfully retrieved skill resources")
                    .build()
            ))
            .onErrorResume(e -> {
                if (e instanceof com.pathprep.exception.ResourceNotFoundException) {
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<SkillResourceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                        )
                    );
                }
                return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SkillResourceResponse>builder()
                        .success(false)
                        .message("Failed to retrieve skill resources: " + e.getMessage())
                        .build()
                    )
                );
            });
    }

    @Operation(
        summary = "Delete skill resources",
        description = "Deletes skill resources by their unique identifier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Successfully deleted skill resources"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Skill resources not found"
        )
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteSkillResources(
            @Parameter(description = "ID of the skill resources to delete")
            @PathVariable String id) {
        log.info("Deleting skill resources with ID: {}", id);
        
        return skillResourceService.deleteSkillResources(id)
            .then(Mono.just(ResponseEntity
                .noContent()
                .<ApiResponse<Void>>build()
            ))
            .onErrorResume(e -> {
                if (e instanceof com.pathprep.exception.ResourceNotFoundException) {
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                        )
                    );
                }
                return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Failed to delete skill resources: " + e.getMessage())
                        .build()
                    )
                );
            });
    }

    @Operation(
        summary = "Refresh skill resources",
        description = "Refreshes skill resources with new AI-generated content"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Successfully refreshed skill resources",
            content = @Content(schema = @Schema(implementation = SkillResourceResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Skill resources not found"
        )
    })
    @PostMapping("/{id}/refresh")
    public Mono<ResponseEntity<ApiResponse<SkillResourceResponse>>> refreshSkillResources(
            @Parameter(description = "ID of the skill resources to refresh")
            @PathVariable String id) {
        log.info("Refreshing skill resources with ID: {}", id);
        
        return skillResourceService.refreshSkillResources(id)
            .map(response -> ResponseEntity.ok(
                ApiResponse.<SkillResourceResponse>builder()
                    .success(true)
                    .data(response)
                    .message("Successfully refreshed skill resources")
                    .build()
            ))
            .onErrorResume(e -> {
                if (e instanceof com.pathprep.exception.ResourceNotFoundException) {
                    return Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.<SkillResourceResponse>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
                        )
                    );
                }
                return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<SkillResourceResponse>builder()
                        .success(false)
                        .message("Failed to refresh skill resources: " + e.getMessage())
                        .build()
                    )
                );
            });
    }
}
