package com.pathprep.model.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pathprep.model.DetailedRoadmap;
import com.pathprep.model.RoadmapPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
@ActiveProfiles("test")
class DetailedRoadmapDeserializerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Configure object mapper if needed
    }

    @Test
    void deserialize_WithArrayOfPhases_ShouldSucceed() throws IOException {
        String json = """
            {
                "role": "Java Developer",
                "experienceLevel": "intermediate",
                "estimatedWeeks": 12,
                "phases": [
                    {
                        "phaseName": "Fundamentals",
                        "weekNumber": "1-2",
                        "objective": "Learn Java basics",
                        "topics": [
                            {
                                "topicName": "Java Syntax",
                                "description": "Basic syntax and structure"
                            }
                        ],
                        "deliverables": ["Complete coding exercises"]
                    },
                    {
                        "title": "Advanced Topics",
                        "weekNumber": 3,
                        "objective": "Learn advanced Java features"
                    }
                ]
            }
            """;

        DetailedRoadmap roadmap = objectMapper.readValue(json, DetailedRoadmap.class);

        assertNotNull(roadmap);
        assertEquals("Java Developer", roadmap.getRole());
        assertEquals("intermediate", roadmap.getExperienceLevel());
        assertEquals(12, roadmap.getEstimatedWeeks());
        
        List<RoadmapPhase> phases = roadmap.getPhases();
        assertNotNull(phases);
        assertEquals(2, phases.size());
        
        RoadmapPhase phase1 = phases.get(0);
        assertEquals("Fundamentals", phase1.getPhaseName());
        assertEquals(1, phase1.getWeekNumber()); // Should parse first number from range
        assertEquals("Learn Java basics", phase1.getObjective());
        assertNotNull(phase1.getTopics());
        assertEquals(1, phase1.getTopics().size());
        assertEquals("Java Syntax", phase1.getTopics().get(0).getTopicName());
        
        RoadmapPhase phase2 = phases.get(1);
        assertEquals("Advanced Topics", phase2.getPhaseName()); // Should map from title
        assertEquals(3, phase2.getWeekNumber());
    }

    @Test
    void deserialize_WithSinglePhaseObject_ShouldSucceed() throws IOException {
        String json = """
            {
                "role": "Data Scientist",
                "experienceLevel": "beginner",
                "phases": {
                    "phaseName": "Introduction",
                    "weekNumber": "1",
                    "objective": "Get started with Data Science"
                }
            }
            """;

        DetailedRoadmap roadmap = objectMapper.readValue(json, DetailedRoadmap.class);

        assertNotNull(roadmap);
        assertEquals("Data Scientist", roadmap.getRole());
        assertEquals("beginner", roadmap.getExperienceLevel());
        
        List<RoadmapPhase> phases = roadmap.getPhases();
        assertNotNull(phases);
        assertEquals(1, phases.size());
        
        RoadmapPhase phase = phases.get(0);
        assertEquals("Introduction", phase.getPhaseName());
        assertEquals(1, phase.getWeekNumber());
        assertEquals("Get started with Data Science", phase.getObjective());
    }

    @Test
    void deserialize_WithInvalidWeekNumber_ShouldUseDefault() throws IOException {
        String json = """
            {
                "role": "Web Developer",
                "experienceLevel": "beginner",
                "phases": [
                    {
                        "phaseName": "HTML/CSS",
                        "weekNumber": "invalid",
                        "objective": "Learn web basics"
                    }
                ]
            }
            """;

        DetailedRoadmap roadmap = objectMapper.readValue(json, DetailedRoadmap.class);
        RoadmapPhase phase = roadmap.getPhases().get(0);
        assertEquals(1, phase.getWeekNumber()); // Should default to 1
    }
}
