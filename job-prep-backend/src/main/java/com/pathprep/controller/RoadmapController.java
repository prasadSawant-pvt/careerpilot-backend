package com.pathprep.controller;

import com.pathprep.dto.ApiResponse;
import com.pathprep.dto.GenerateRoadmapRequest;
import com.pathprep.dto.GroqQueryRequest;
import com.pathprep.model.Roadmap;
import com.pathprep.model.Role;
import com.pathprep.model.Skill;
import com.pathprep.service.RoadmapService;
import com.pathprep.service.RoleService;
import com.pathprep.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Roadmap", description = "APIs for generating and managing learning roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final RoleService roleService;
    private final SkillService skillService;

    public RoadmapController(RoadmapService roadmapService, RoleService roleService, SkillService skillService) {
        this.roadmapService = roadmapService;
        this.roleService = roleService;
        this.skillService = skillService;
    }

    @PostMapping("/ai/generate-roadmap")
    @Operation(summary = "Generate a new learning roadmap using AI")
    public Mono<ResponseEntity<ApiResponse<Roadmap>>> generateRoadmap(
            @Valid @RequestBody GenerateRoadmapRequest request) {

        return roadmapService.generateRoadmap(request.getRole(), request.getExperience(), request.getSkills())
                .map(roadmap -> ResponseEntity.ok(
                        ApiResponse.success("Roadmap generated successfully", roadmap)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }

    @GetMapping("/roadmaps/{id}")
    @Operation(summary = "Get a roadmap by ID")
    public Mono<ResponseEntity<ApiResponse<Roadmap>>> getRoadmap(@PathVariable String id) {
        return roadmapService.getRoadmap(id)
                .map(roadmap -> ResponseEntity.ok(
                        ApiResponse.success("Roadmap retrieved successfully", roadmap)
                ))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping("/roadmaps/recent")
    @Operation(summary = "Get most recently generated roadmaps")
    public Mono<ResponseEntity<ApiResponse<List<Roadmap>>>> getRecentRoadmaps(
            @RequestParam(defaultValue = "5") int limit) {

        return roadmapService.getRecentRoadmaps(limit)
                .collectList()
                .map(roadmaps -> ResponseEntity.ok(
                        ApiResponse.success("Recent roadmaps retrieved successfully", roadmaps)
                ));
    }

    @GetMapping("/roadmaps/trending")
    @Operation(summary = "Get trending roadmaps")
    public Mono<ResponseEntity<ApiResponse<List<Roadmap>>>> getTrendingRoadmaps(
            @RequestParam(defaultValue = "5") int limit) {

        return roadmapService.getTrendingRoadmaps(limit)
                .collectList()
                .map(roadmaps -> ResponseEntity.ok(
                        ApiResponse.success("Trending roadmaps retrieved successfully", roadmaps)
                ));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all available roles")
    public Mono<ResponseEntity<ApiResponse<List<Role>>>> getAllRoles() {
        return roleService.findAllRoles()
                .collectList()
                .map(roles -> ResponseEntity.ok(
                        ApiResponse.success("Roles retrieved successfully", roles)
                ));
    }

    @GetMapping("/skills/search")
    @Operation(summary = "Search for skills")
    public Mono<ResponseEntity<ApiResponse<List<Skill>>>> searchSkills(@RequestParam String query) {
        return skillService.searchSkills(query)
                .collectList()
                .map(skills -> ResponseEntity.ok(
                        ApiResponse.success("Skills retrieved successfully", skills)
                ));
    }

    @PostMapping("/ai/query")
    @Operation(
            summary = "Directly query the Groq model",
            description = "Send a prompt directly to the Groq model and get a response without any database interaction"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully queried the Groq model",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"status\":\"success\",\"message\":\"Query successful\",\"data\":\"Generated response from Groq model...\"}"
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or error querying the model",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"status\":\"error\",\"message\":\"Failed to query Groq model: Error details...\"}"
                            )
                    )
            )
    })
    public Mono<ResponseEntity<ApiResponse<String>>> queryGroqModel(
            @Valid @RequestBody GroqQueryRequest request) {

        return roadmapService.queryGroqModel(request.getPrompt(), request.getModel())
                .map(response -> ResponseEntity.ok(
                        ApiResponse.success("Query successful", response)
                ))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest()
                        .body(ApiResponse.error(e.getMessage()))));
    }
}