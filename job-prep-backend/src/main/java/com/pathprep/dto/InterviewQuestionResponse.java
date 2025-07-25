package com.pathprep.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing interview questions")
public class InterviewQuestionResponse {
    
    @Schema(description = "Job role for which questions were generated", example = "Java Developer")
    private String role;
    
    @Schema(description = "Experience level", example = "Mid")
    private String experienceLevel;
    
    @ArraySchema(
        arraySchema = @Schema(description = "List of generated questions"),
        schema = @Schema(implementation = QuestionItem.class)
    )
    private List<QuestionItem> questions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Individual interview question with answer")
    public static class QuestionItem {
        
        @Schema(description = "The interview question", 
               example = "Explain the difference between @Component, @Service, and @Repository annotations in Spring")
        private String question;
        
        @Schema(description = "Answer to the question",
               example = "@Component is a generic stereotype for any Spring-managed component. @Service indicates that a class contains business logic. @Repository is used for data access layer and provides exception translation from database exceptions to Spring's DataAccessException hierarchy.")
        private String answer;
        
        @Schema(description = "Category of the question (e.g., 'Spring', 'Java Core', 'System Design')",
               example = "Spring Framework")
        private String category;
        
        @Schema(description = "Difficulty level of the question (e.g., 'Easy', 'Medium', 'Hard')",
               example = "Medium")
        private String difficulty;
    }
}
