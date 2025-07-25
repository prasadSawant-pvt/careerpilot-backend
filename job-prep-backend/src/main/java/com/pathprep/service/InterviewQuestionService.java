package com.pathprep.service;

import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
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
     * @return A Mono emitting the interview questions
     */
    Mono<InterviewQuestionResponse> getQuestions(String role, String experienceLevel, int count);
}
