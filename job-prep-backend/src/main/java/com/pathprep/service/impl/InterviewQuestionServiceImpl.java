package com.pathprep.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.pathprep.config.GroqProperties;
import com.pathprep.dto.GenerateQuestionsRequest;
import com.pathprep.dto.InterviewQuestionResponse;
import com.pathprep.dto.SkillQuestionsRequest;
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
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    public Mono<InterviewQuestionResponse> getQuestions(String role, String experienceLevel, int count, boolean forceRefresh) {
        log.info("Getting {} questions for {} ({}), forceRefresh: {}", count, role, experienceLevel, forceRefresh);
        
        if (forceRefresh) {
            log.debug("Force refresh requested, bypassing cache");
            GenerateQuestionsRequest request = new GenerateQuestionsRequest();
            request.setRole(role);
            request.setExperienceLevel(experienceLevel);
            request.setCount(count);
            request.setForceRefresh(true);
            return generateQuestions(request);
        }
        
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
    
    @Override
    @Cacheable(
        value = "skillInterviewQuestions",
        key = "#request.skill + '-' + #request.jobRole + '-' + #request.experienceLevel + '-' + #request.count",
        unless = "#result == null || #result.questions == null || #result.questions.size() < #request.count"
    )
    public Mono<InterviewQuestionResponse> generateSkillQuestions(SkillQuestionsRequest request) {
        // If forceRefresh is true, bypass cache and generate new questions
        if (request.isForceRefresh()) {
            log.info("Force refresh requested for skill questions, bypassing cache");
            return generateSkillQuestionsWithAI(request);
        }
        log.info("Generating {} questions for skill: {}, role: {}, experience: {}", 
                request.getCount(), request.getSkill(), request.getJobRole(), request.getExperienceLevel());
        
        // First try to get from database
        return getSkillQuestionsFromDb(
                    request.getJobRole(), 
                    request.getExperienceLevel(), 
                    request.getSkill(), 
                    request.getCount()
                )
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("No questions found in DB for skill: {}, generating with AI", request.getSkill());
                    return generateSkillQuestionsWithAI(request);
                }));
    }
    
    private Mono<InterviewQuestionResponse> getSkillQuestionsFromDb(String role, String experienceLevel, String skill, int count) {
        log.debug("Fetching up to {} questions for skill: {}, role: {}, experience: {} from database", 
                count, skill, role, experienceLevel);
        
        return Mono.fromCallable(() -> {
                    List<InterviewQuestion> questions = questionRepository.findByRoleAndExperienceAndSkill(
                            role, experienceLevel, skill);
                    
                    if (questions == null || questions.isEmpty()) {
                        return Collections.<InterviewQuestion>emptyList();
                    }
                    
                    // If we have enough questions, return a random sample of the requested count
                    if (questions.size() >= count) {
                        Collections.shuffle(questions);
                        return questions.subList(0, Math.min(count, questions.size()));
                    }
                    
                    return questions;
                })
                .timeout(DB_TIMEOUT)
                .flatMap(questions -> {
                    if (questions.isEmpty()) {
                        return Mono.empty();
                    }
                    
                    log.debug("Found {} questions in database for skill: {}, role: {}, experience: {}", 
                            questions.size(), skill, role, experienceLevel);
                    
                    return Mono.just(mapToResponse(role, experienceLevel, questions));
                })
                .onErrorResume(e -> {
                    log.error("Error fetching skill questions from database", e);
                    return Mono.empty();
                });
    }
    
    private Mono<InterviewQuestionResponse> generateSkillQuestionsWithAI(SkillQuestionsRequest request) {
        String skill = request.getSkill();
        String role = request.getJobRole();
        String experience = request.getExperienceLevel();
        int questionCount = Math.min(request.getCount(), 20); // Cap at 20 questions max per request
        
        String prompt = String.format("""
                Generate exactly %d unique interview questions for a %s position at the %s level 
                specifically focusing on %s.
                
                IMPORTANT: You MUST return EXACTLY %d questions in the 'questions' array. 
                Do not return fewer or more questions than requested.
                
                For each question, include a detailed answer that would be expected from a candidate 
                with %s years of experience. The questions should be technical and specific to %s.
                
                Format your response as a JSON object with a 'questions' array. Each item in the array should have:
                {
                    "questions": [
                        {
                            "question": "The interview question",
                            "answer": "A detailed answer",
                            "category": "The category (e.g., 'Core', 'Advanced', 'Best Practices')",
                            "difficulty": "The difficulty level ('Easy', 'Medium', 'Hard')"
                        },
                        ... more questions ...
                    ]
                }
                
                Make sure to:
                1. Generate exactly %d questions
                2. Return a valid JSON object with a 'questions' array
                3. Each question should be unique and relevant to %s
                4. Include detailed answers with code examples where appropriate
                
                Example:
                {
                    "questions": [
                        {
                            "question": "What is the difference between @Component and @Service in Spring?",
                            "answer": "Both @Component and @Service are Spring stereotypes...",
                            "category": "Core",
                            "difficulty": "Easy"
                        }
                    ]
                }
                    },
                    ...
                ]
                """, 
                questionCount, role, experience, skill, questionCount, experience, skill, questionCount, skill);
        
        log.debug("Generating questions with prompt: {}", prompt);
        
        return groqAIService.generateText(prompt, groqProperties.getDefaultModel())
            .timeout(AI_TIMEOUT)
            .flatMap(aiResponse -> {
                try {
                    log.debug("Raw AI response: {}", aiResponse);
                    
                    // Clean and extract JSON from the AI response
                    String jsonResponse = aiResponse.trim();
                    
                    // Handle markdown code blocks
                    if (jsonResponse.startsWith("```json")) {
                        jsonResponse = jsonResponse.substring(jsonResponse.indexOf("\n") + 1);
                        jsonResponse = jsonResponse.substring(0, jsonResponse.lastIndexOf("```")).trim();
                    } 
                    // Handle code blocks without json specifier
                    else if (jsonResponse.startsWith("```")) {
                        jsonResponse = jsonResponse.substring(jsonResponse.indexOf("\n") + 1);
                        jsonResponse = jsonResponse.substring(0, jsonResponse.lastIndexOf("```")).trim();
                    }
                    // Handle responses that start with text before JSON
                    else if (jsonResponse.startsWith("Here") || jsonResponse.startsWith("here")) {
                        // Try to find the first { or [ that starts the JSON
                        int jsonStart = Math.max(
                            jsonResponse.indexOf('{'),
                            jsonResponse.indexOf('[')
                        );
                        if (jsonStart > 0) {
                            jsonResponse = jsonResponse.substring(jsonStart).trim();
                        }
                    }
                    
                    // Parse the AI response
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    
                    List<Map<String, String>> questionMaps = new ArrayList<>();
                    
                    // Handle both array and object responses
                    if (rootNode.isArray()) {
                        // If the response is an array, convert it to a list of maps
                        questionMaps = objectMapper.convertValue(rootNode, new TypeReference<List<Map<String, String>>>() {});
                    } else if (rootNode.has("questions") && rootNode.get("questions").isArray()) {
                        // If the response is an object with a 'questions' array
                        questionMaps = objectMapper.convertValue(rootNode.get("questions"), new TypeReference<List<Map<String, String>>>() {});
                    } else if (rootNode.has("data") && rootNode.get("data").isArray()) {
                        // If the response is an object with a 'data' array
                        questionMaps = objectMapper.convertValue(rootNode.get("data"), new TypeReference<List<Map<String, String>>>() {});
                    } else {
                        // If it's a single question object, wrap it in a list
                        Map<String, String> singleQuestion = objectMapper.convertValue(rootNode, new TypeReference<Map<String, String>>() {});
                        questionMaps = List.of(singleQuestion);
                    }
                    
                    if (questionMaps == null || questionMaps.isEmpty()) {
                        return Mono.error(new AIServiceException("No questions were generated by the AI"));
                    }
                    
                    // Map to our domain model
                    List<InterviewQuestion> questions = questionMaps.stream()
                            .map(qMap -> {
                                InterviewQuestion question = new InterviewQuestion();
                                question.setQuestion(qMap.get("question"));
                                question.setAnswer(qMap.get("answer"));
                                question.setCategory(qMap.getOrDefault("category", "General"));
                                question.setDifficulty(qMap.getOrDefault("difficulty", "Medium"));
                                question.setRole(role);
                                question.setExperience(experience);
                                question.setSkill(skill);
                                question.setTags(List.of(skill, role.toLowerCase()));
                                return question;
                            })
                            .collect(Collectors.toList());
                    
                    // Save to database for future use
                    log.info("Saving {} generated questions for skill: {} to database", questions.size(), skill);
                    return Mono.fromCallable(() -> questionRepository.saveAll(questions))
                            .thenReturn(mapToResponse(role, experience, questions));
                            
                } catch (Exception e) {
                    log.error("Error parsing AI response for skill questions", e);
                    return Mono.error(new AIServiceException("Failed to parse AI response: " + e.getMessage(), e));
                }
            })
            .onErrorResume(e -> {
                log.error("Error in generateSkillQuestionsWithAI: {}", e.getMessage(), e);
                if (e instanceof AIServiceException) {
                    return Mono.error(e);
                }
                return Mono.error(new AIServiceException("Failed to generate questions: " + e.getMessage(), e));
            });
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
