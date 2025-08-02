package com.pathprep.service;

import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
import com.pathprep.dto.SkillQuestionsRequest;
import reactor.core.publisher.Mono;

public interface InterviewQuestionService {
    /**
     * Generate interview questions based on the provided request
     * 
     * @param request The request containing role, experience level, and other parameters
     * @return A Mono emitting the generated interview questions
     */
    Mono<InterviewQuestionResponse> generateQuestions(GenerateQuestionsRequest request);
    
    /**
     * Get interview questions from the database
     * 
     * @param role The job role
     * @param experienceLevel The experience level
     * @param count Maximum number of questions to return
     * @param forceRefresh Whether to force refresh the cache
     * @return A Mono emitting the interview questions
     */
    Mono<InterviewQuestionResponse> getQuestions(String role, String experienceLevel, int count, boolean forceRefresh);
    
    /**
     * Generate skill-specific interview questions
     * 
     * @param request The request containing skill, job role, experience level, and question count
     * @return A Mono emitting the generated interview questions for the specific skill
     */
    Mono<InterviewQuestionResponse> generateSkillQuestions(SkillQuestionsRequest request);
}
