package com.pathprep.service.impl;

import com.pathprep.config.GroqProperties;
import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
import com.pathprep.exception.AIServiceException;
import com.pathprep.model.InterviewQuestion;
import com.pathprep.repository.InterviewQuestionRepository;
import com.pathprep.service.GroqAIService;
import com.pathprep.service.InterviewQuestionService;
import com.pathprep.util.ModelMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {
    
    private static final Duration DB_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);
    
    private final InterviewQuestionRepository questionRepository;
    private final GroqAIService groqAIService;
    private final GroqProperties groqProperties;
    private final ModelMapperUtil modelMapper;
    
    @Override
    @Cacheable(
        value = "interviewQuestions", 
        key = "T(java.util.Objects).hash(T(org.apache.commons.lang3.StringUtils).defaultString(#request.role, ''), T(org.apache.commons.lang3.StringUtils).defaultString(#request.experienceLevel, ''), #request.count, T(org.apache.commons.lang3.StringUtils).defaultString(#request.topics, ''))",
        unless = "#result == null || #result.questions == null || #result.questions.size() < #request.count || (#request.forceRefresh != null && #request.forceRefresh)",
        condition = "#request.forceRefresh == null || !#request.forceRefresh"
    )
    @CacheEvict(
        value = "interviewQuestions", 
        key = "T(java.util.Objects).hash(T(org.apache.commons.lang3.StringUtils).defaultString(#request.role, ''), T(org.apache.commons.lang3.StringUtils).defaultString(#request.experienceLevel, ''), #request.count, T(org.apache.commons.lang3.StringUtils).defaultString(#request.topics, ''))",
        beforeInvocation = true, 
        condition = "#request.forceRefresh != null && #request.forceRefresh"
    )
    public Mono<InterviewQuestionResponse> generateQuestions(GenerateQuestionsRequest request) {
        // Validate request
        if (request == null || request.getRole() == null || request.getExperienceLevel() == null) {
            return Mono.error(new IllegalArgumentException("Request, role, and experience level are required"));
        }
        
        // Ensure count is within reasonable bounds
        int count = Math.min(Math.max(1, request.getCount()), 100); // 1-100 questions
        if (count != request.getCount()) {
            log.info("Adjusted question count from {} to {}", request.getCount(), count);
            request.setCount(count);
        }
        
        log.info("Generating {} interview questions for {} ({} level) with topics: {}", 
                count, request.getRole(), request.getExperienceLevel(), 
                request.getTopics() != null ? request.getTopics() : "general");
        
        // First try to get questions from database
        return getQuestionsFromDb(request.getRole(), request.getExperienceLevel(), count)
                .switchIfEmpty(Mono.defer(() -> {
                    // If not enough questions in DB, generate with AI
                    log.info("Not enough questions in DB, generating with AI");
                    return generateQuestionsWithAI(request);
                }))
                .doOnSuccess(response -> {
                    if (response != null && response.getQuestions() != null) {
                        log.debug("Successfully generated {} questions for {} ({})", 
                                response.getQuestions().size(), request.getRole(), request.getExperienceLevel());
                    } else {
                        log.warn("Failed to generate questions for {} ({})", 
                                request.getRole(), request.getExperienceLevel());
                    }
                });
    }
    
    @Override
    public Mono<InterviewQuestionResponse> getQuestions(String role, String experienceLevel, int count) {
        log.info("Getting {} questions for {} ({})", count, role, experienceLevel);
        return getQuestionsFromDb(role, experienceLevel, count)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No questions found in DB for {} ({}), generating with AI", role, experienceLevel);
                    GenerateQuestionsRequest request = new GenerateQuestionsRequest();
                    request.setRole(role);
                    request.setExperienceLevel(experienceLevel);
                    request.setCount(count);
                    return generateQuestions(request);
                }));
    }
    
    private Mono<InterviewQuestionResponse> getQuestionsFromDb(String role, String experienceLevel, int count) {
        log.debug("Fetching up to {} questions for {} ({}) from database", count, role, experienceLevel);
        
        return Mono.fromCallable(() -> {
                    // First try to get exact count if possible
                    List<InterviewQuestion> questions = questionRepository.findByRoleAndExperience(role, experienceLevel);
                    
                    if (questions == null || questions.isEmpty()) {
                        return Collections.<InterviewQuestion>emptyList();
                    }
                    
                    // If we have enough questions, return a random sample of the requested count
                    if (questions.size() >= count) {
                        Collections.shuffle(questions);
                        return questions.stream().limit(count).collect(Collectors.toList());
                    }
                    
                    // If we don't have enough questions, return all we have
                    log.info("Only found {} questions in database for {} ({}), which is less than requested {}", 
                            questions.size(), role, experienceLevel, count);
                    return questions;
                })
                .timeout(DB_TIMEOUT)
                .flatMap(questions -> {
                    if (questions.isEmpty()) {
                        log.debug("No questions found in database for {} ({})", role, experienceLevel);
                        return Mono.empty();
                    }
                    
                    log.debug("Found {} questions in database for {} ({})", 
                            questions.size(), role, experienceLevel);
                    
                    // Map to response
                    return Mono.just(mapToResponse(role, experienceLevel, questions));
                })
                .onErrorResume(e -> {
                    log.error("Error fetching questions from database", e);
                    return Mono.empty();
                });
    }
    
    private Mono<InterviewQuestionResponse> generateQuestionsWithAI(GenerateQuestionsRequest request) {
        String topics = request.getTopics() != null ? request.getTopics() : "general";
        int questionCount = Math.min(request.getCount(), 100); // Cap at 100 questions max per request
        
        String prompt = String.format("""
                Generate exactly %d unique interview questions for a %s position at the %s level.
                Focus on these topics: %s
                
                IMPORTANT: You MUST return exactly %d questions. Do not return fewer or more questions than requested.
                
                Return the response as a JSON object with a 'questions' array containing objects with these fields:
                - question: The interview question (required)
                - answer: A detailed answer (at least 2-3 sentences)
                - category: The category of the question (e.g., 'Java', 'Spring', 'System Design')
                - difficulty: The difficulty level ('Easy', 'Medium', 'Hard')
                
                Example response format:
                {
                  "role": "Java Developer",
                  "experienceLevel": "Beginner",
                  "questions": [
                    {
                      "question": "What is Java?",
                      "answer": "Java is a high-level, class-based, object-oriented programming language that is designed to have as few implementation dependencies as possible. It is a general-purpose programming language intended to let programmers write once, run anywhere (WORA), meaning that compiled Java code can run on all platforms that support Java without the need for recompilation.",
                      "category": "Java Core",
                      "difficulty": "Easy"
                    }
                  ]
                }
                """,
                questionCount,
                request.getRole(),
                request.getExperienceLevel(),
                topics,
                questionCount);
        
        log.info("Sending prompt to AI: {}", prompt);
        String model = groqProperties.getDefaultModel();
        
        return groqAIService.generateStructuredResponse(prompt, model, InterviewQuestionResponse.class)
                .timeout(AI_TIMEOUT)
                .flatMap(response -> {
                    if (response == null || response.getQuestions() == null || response.getQuestions().isEmpty()) {
                        log.error("AI returned null or empty questions list");
                        return Mono.error(new AIServiceException("AI returned invalid response format"));
                    }
                    
                    log.info("Received {} questions from AI", response.getQuestions().size());
                    
                    // Save the generated questions to the database
                    List<InterviewQuestion> questions = response.getQuestions().stream()
                            .filter(q -> q != null && q.getQuestion() != null && !q.getQuestion().trim().isEmpty())
                            .map(q -> {
                                InterviewQuestion question = new InterviewQuestion();
                                question.setRole(request.getRole());
                                question.setExperience(request.getExperienceLevel());
                                question.setQuestion(q.getQuestion().trim());
                                question.setAnswer(q.getAnswer() != null ? q.getAnswer().trim() : "");
                                question.setCategory(q.getCategory() != null ? q.getCategory() : "General");
                                question.setDifficulty(q.getDifficulty() != null ? q.getDifficulty() : "Medium");
                                question.setTags(List.of(question.getCategory()));
                                return question;
                            })
                            .collect(Collectors.toList());
                    
                    if (questions.isEmpty()) {
                        log.error("No valid questions could be extracted from AI response");
                        return Mono.error(new AIServiceException("No valid questions could be generated"));
                    }
                    
                    log.info("Saving {} valid questions to database", questions.size());
                    return Mono.fromCallable(() -> questionRepository.saveAll(questions))
                            .thenReturn(response);
                })
                .onErrorResume(e -> {
                    log.error("Error in generateQuestionsWithAI: {}", e.getMessage(), e);
                    if (e instanceof AIServiceException) {
                        return Mono.error(e);
                    }
                    return Mono.error(new AIServiceException("Failed to generate questions: " + e.getMessage(), e));
                });
    }
    
    private InterviewQuestionResponse mapToResponse(String role, String experienceLevel, List<InterviewQuestion> questions) {
        List<InterviewQuestionResponse.QuestionItem> items = questions.stream()
                .map(q -> InterviewQuestionResponse.QuestionItem.builder()
                        .question(q.getQuestion())
                        .answer(q.getAnswer())
                        .category(q.getCategory())
                        .difficulty(q.getDifficulty())
                        .build())
                .collect(Collectors.toList());
        
        return InterviewQuestionResponse.builder()
                .role(role)
                .experienceLevel(experienceLevel)
                .questions(items)
                .build();
    }
}
