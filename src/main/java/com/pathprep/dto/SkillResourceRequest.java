package com.pathprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for skill resource generation
 */
@Data
public class SkillResourceRequest {
    @NotBlank(message = "Skill name is required")
    private String skillName;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    @NotBlank(message = "Experience level is required")
    private String experienceLevel;
    
    @NotNull(message = "Include learning paths flag is required")
    private Boolean includeLearningPaths = true;
    
    @NotNull(message = "Include projects flag is required")
    private Boolean includeProjects = true;
    
    @NotNull(message = "Include certifications flag is required")
    private Boolean includeCertifications = true;
    
    @NotNull(message = "Include communities flag is required")
    private Boolean includeCommunities = true;
    
    // Maximum number of resources to return per category
    private Integer maxResourcesPerCategory = 5;
}
