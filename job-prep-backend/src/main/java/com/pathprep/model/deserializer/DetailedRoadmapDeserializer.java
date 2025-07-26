package com.pathprep.model.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.pathprep.model.DetailedRoadmap;
import com.pathprep.model.RoadmapPhase;
import com.pathprep.model.Topic;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.time.LocalDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pathprep.service.impl.RoadmapServiceImpl.log;

/**
 * Custom deserializer for DetailedRoadmap that handles both array and object formats
 * for the phases field, and properly processes week number ranges.
 */
public class DetailedRoadmapDeserializer extends StdDeserializer<DetailedRoadmap> implements ContextualDeserializer {

    private static final long serialVersionUID = 1L;
    private final ObjectMapper objectMapper;

    public DetailedRoadmapDeserializer() {
        this(null);
    }

    protected DetailedRoadmapDeserializer(Class<?> vc) {
        super(vc);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public DetailedRoadmap deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        log.debug("Deserializing roadmap from JSON: {}", node);
        
        DetailedRoadmap roadmap = new DetailedRoadmap();
        
        try {
            // Handle both direct fields and nested 'data' object
            JsonNode dataNode = node.has("data") ? node.get("data") : node;
            
            // Set basic fields
            if (dataNode.has("role")) {
                roadmap.setRole(dataNode.get("role").asText());
            }
            if (dataNode.has("experienceLevel")) {
                roadmap.setExperienceLevel(dataNode.get("experienceLevel").asText());
            }
            if (dataNode.has("estimatedWeeks")) {
                roadmap.setEstimatedWeeks(dataNode.get("estimatedWeeks").asInt());
            }
            
            // Handle phases - support multiple possible field names and formats
            List<RoadmapPhase> phases = extractPhases(dataNode);
            
            // Set phases if we found any
            if (!phases.isEmpty()) {
                // Sort phases by week number
                phases.sort(Comparator.comparingInt(phase -> 
                    phase.getWeekNumber() != null ? phase.getWeekNumber() : 0));
                    
                roadmap.setPhases(phases);
                
                // Update estimated weeks based on actual phases if not set
                if (roadmap.getEstimatedWeeks() == null || roadmap.getEstimatedWeeks() == 0) {
                    int maxWeek = phases.stream()
                        .mapToInt(phase -> phase.getWeekNumber() != null ? phase.getWeekNumber() : 0)
                        .max()
                        .orElse(0);
                    roadmap.setEstimatedWeeks(maxWeek > 0 ? maxWeek : 1);
                }
            } else {
                log.warn("No valid phases found in the AI response");
            }
            
            // Set composite key if we have required fields
            if (roadmap.getRole() != null && roadmap.getExperienceLevel() != null) {
                roadmap.setCompositeKey();
            }
            
            return roadmap;
            
        } catch (Exception e) {
            log.error("Error deserializing roadmap: {}", e.getMessage(), e);
            throw new IOException("Failed to deserialize roadmap: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts phases from a JSON node, handling various formats and field names.
     */
    private List<RoadmapPhase> extractPhases(JsonNode dataNode) {
        List<RoadmapPhase> phases = new ArrayList<>();
        
        // Handle phases - support multiple possible field names and formats
        String[] possiblePhaseFields = {"phases", "learningPhases", "roadmapPhases", "stages"};
        
        for (String field : possiblePhaseFields) {
            if (dataNode.has(field)) {
                JsonNode phasesNode = dataNode.get(field);
                
                if (phasesNode.isArray()) {
                    // Handle array of phases
                    for (JsonNode phaseNode : phasesNode) {
                        try {
                            RoadmapPhase phase = parsePhase(phaseNode);
                            if (phase != null) {
                                phases.add(phase);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse phase in array: {}", phaseNode, e);
                        }
                    }
                } else if (phasesNode.isObject()) {
                    // Handle single phase object
                    try {
                        RoadmapPhase phase = parsePhase(phasesNode);
                        if (phase != null) {
                            phases.add(phase);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse phase object: {}", phasesNode, e);
                    }
                }
                
                // If we found phases, no need to check other field names
                if (!phases.isEmpty()) {
                    break;
                }
            }
        }
        
        // If no phases found, try to find them at the root level
        if (phases.isEmpty()) {
            if (dataNode.isArray()) {
                // Handle case where the entire response is an array of phases
                for (JsonNode phaseNode : dataNode) {
                    try {
                        RoadmapPhase phase = parsePhase(phaseNode);
                        if (phase != null) {
                            phases.add(phase);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse phase in root array: {}", phaseNode, e);
                    }
                }
            } else if (dataNode.isObject()) {
                // Handle case where phases are direct properties of the root object
                phases = extractPhasesFromObject(dataNode);
            }
        }
        
        return phases;
    }

    /**
     * Parses a phase from a JSON node, handling various formats and edge cases.
     */
    private RoadmapPhase parsePhase(JsonNode phaseNode) {
        if (phaseNode == null || phaseNode.isNull()) {
            return null;
        }
        
        try {
            RoadmapPhase phase = new RoadmapPhase();
            
            // Handle phaseName (with multiple possible field names)
            String[] possibleNameFields = {"phaseName", "title", "name", "phase", "week"};
            for (String field : possibleNameFields) {
                if (phaseNode.has(field) && phaseNode.get(field).isTextual()) {
                    String name = phaseNode.get(field).asText().trim();
                    if (!name.isEmpty()) {
                        phase.setPhaseName(name);
                        break;
                    }
                }
            }
            
            // Handle weekNumber - can be number, string, or range string
            String[] possibleWeekFields = {"weekNumber", "week", "weekNum", "phaseNumber"};
            boolean weekNumberSet = false;
            
            for (String weekField : possibleWeekFields) {
                if (phaseNode.has(weekField)) {
                    JsonNode weekNode = phaseNode.get(weekField);
                    try {
                        if (weekNode.isNumber()) {
                            // Handle direct number
                            phase.setWeekNumber(weekNode.asInt());
                            weekNumberSet = true;
                            break;
                        } else if (weekNode.isTextual()) {
                            // Handle string format (e.g., "2-4" or "6-7" or "Week 1")
                            String weekStr = weekNode.asText().trim();
                            
                            // Handle "Week X" format
                            if (weekStr.matches("(?i)week\\s*\\d+")) {
                                weekStr = weekStr.replaceAll("(?i)week\\s*", "").trim();
                            }
                            
                            // Handle ranges like "2-4" by taking the first number
                            if (weekStr.contains("-")) {
                                String[] parts = weekStr.split("-");
                                if (parts.length > 0) {
                                    weekStr = parts[0].trim();
                                }
                            }
                            
                            if (!weekStr.isEmpty()) {
                                phase.setWeekNumber(Integer.parseInt(weekStr));
                                weekNumberSet = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Could not parse week number from field '{}': {}", weekField, weekNode);
                        // Continue to next possible field
                    }
                }
            }
            
            // If no week number was set, try to infer it from the phase name
            if (!weekNumberSet && phase.getPhaseName() != null) {
                try {
                    // Look for patterns like "Week 1" or "Phase 2" in the phase name
                    java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("(?i)(?:week|phase)\\s*(\\d+)")
                        .matcher(phase.getPhaseName());
                    if (matcher.find()) {
                        phase.setWeekNumber(Integer.parseInt(matcher.group(1)));
                    }
                } catch (Exception e) {
                    log.debug("Could not infer week number from phase name: {}", phase.getPhaseName());
                }
            }
            
            // Default to 1 if no week number was found
            if (phase.getWeekNumber() == null || phase.getWeekNumber() < 1) {
                phase.setWeekNumber(1);
            }
            
            // Handle objective/description
            String[] possibleObjectiveFields = {"objective", "description", "summary"};
            for (String field : possibleObjectiveFields) {
                if (phaseNode.has(field) && phaseNode.get(field).isTextual()) {
                    String objective = phaseNode.get(field).asText().trim();
                    if (!objective.isEmpty()) {
                        phase.setObjective(objective);
                        break;
                    }
                }
            }
            
            // Handle topics
            String[] possibleTopicFields = {"topics", "learningTopics", "subjects"};
            for (String field : possibleTopicFields) {
                if (phaseNode.has(field)) {
                    try {
                        JsonNode topicsNode = phaseNode.get(field);
                        if (topicsNode.isArray() && topicsNode.size() > 0) {
                            List<Topic> topics = new ArrayList<>();
                            for (JsonNode topicNode : topicsNode) {
                                try {
                                    Topic topic = parseTopic(topicNode);
                                    if (topic != null) {
                                        topics.add(topic);
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to parse topic: {}", topicNode, e);
                                }
                            }
                            if (!topics.isEmpty()) {
                                phase.setTopics(topics);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing topics from field '{}': {}", field, e.getMessage());
                    }
                }
            }
            
            // Handle deliverables
            String[] possibleDeliverableFields = {"deliverables", "outcomes", "results"};
            for (String field : possibleDeliverableFields) {
                if (phaseNode.has(field)) {
                    try {
                        JsonNode deliverablesNode = phaseNode.get(field);
                        if (deliverablesNode.isArray() && deliverablesNode.size() > 0) {
                            List<String> deliverables = new ArrayList<>();
                            for (JsonNode deliverableNode : deliverablesNode) {
                                if (deliverableNode.isTextual()) {
                                    String deliverable = deliverableNode.asText().trim();
                                    if (!deliverable.isEmpty()) {
                                        deliverables.add(deliverable);
                                    }
                                }
                            }
                            if (!deliverables.isEmpty()) {
                                phase.setDeliverables(deliverables);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing deliverables from field '{}': {}", field, e.getMessage());
                    }
                }
            }
            
            return phase;
            
        } catch (Exception e) {
            log.error("Error parsing phase: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts phases from a JSON object by looking for phase-like structures in its fields.
     */
    private List<RoadmapPhase> extractPhasesFromObject(JsonNode node) {
        List<RoadmapPhase> phases = new ArrayList<>();
        
        // Check if this object looks like a phase itself
        if (isPhaseLike(node)) {
            try {
                RoadmapPhase phase = parsePhase(node);
                if (phase != null) {
                    phases.add(phase);
                }
            } catch (Exception e) {
                log.warn("Failed to parse phase from object: {}", node, e);
            }
            return phases;
        }
        
        // Otherwise, look for phase-like objects in the fields
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            
            if (value.isObject() && isPhaseLike(value)) {
                try {
                    RoadmapPhase phase = parsePhase(value);
                    if (phase != null) {
                        // If the phase doesn't have a name, use the field name
                        if (phase.getPhaseName() == null || phase.getPhaseName().isEmpty()) {
                            phase.setPhaseName(entry.getKey());
                        }
                        phases.add(phase);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse phase from field '{}': {}", entry.getKey(), value, e);
                }
            } else if (value.isArray()) {
                // Check if array contains phase-like objects
                for (JsonNode arrayItem : value) {
                    if (arrayItem.isObject() && isPhaseLike(arrayItem)) {
                        try {
                            RoadmapPhase phase = parsePhase(arrayItem);
                            if (phase != null) {
                                phases.add(phase);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse phase from array item: {}", arrayItem, e);
                        }
                    }
                }
            }
        }
        
        return phases;
    }
    
    /**
     * Parses a topic from a JSON node, handling various formats and edge cases.
     */
    private Topic parseTopic(JsonNode topicNode) {
        if (topicNode == null || topicNode.isNull()) {
            return null;
        }
        
        try {
            Topic topic = new Topic();
            
            // Handle topicName (with multiple possible field names)
            String[] possibleNameFields = {"topicName", "name", "title", "skill", "concept"};
            for (String field : possibleNameFields) {
                if (topicNode.has(field) && topicNode.get(field).isTextual()) {
                    String name = topicNode.get(field).asText().trim();
                    if (!name.isEmpty()) {
                        topic.setTopicName(name);
                        break;
                    }
                }
            }
            
            // Handle description
            String[] possibleDescFields = {"description", "desc", "details", "summary"};
            for (String field : possibleDescFields) {
                if (topicNode.has(field) && topicNode.get(field).isTextual()) {
                    String desc = topicNode.get(field).asText().trim();
                    if (!desc.isEmpty()) {
                        topic.setDescription(desc);
                        break;
                    }
                }
            }
            
            // Handle estimatedHours
            String[] possibleHourFields = {"estimatedHours", "hours", "timeRequired", "duration"};
            for (String field : possibleHourFields) {
                if (topicNode.has(field)) {
                    JsonNode hoursNode = topicNode.get(field);
                    try {
                        if (hoursNode.isNumber()) {
                            topic.setEstimatedHours(hoursNode.asInt());
                            break;
                        } else if (hoursNode.isTextual()) {
                            String hoursStr = hoursNode.asText().trim();
                            if (!hoursStr.isEmpty()) {
                                // Handle ranges like "2-4" by taking the average
                                if (hoursStr.contains("-")) {
                                    String[] parts = hoursStr.split("-");
                                    if (parts.length == 2) {
                                        double avg = (Integer.parseInt(parts[0].trim()) + 
                                                    Integer.parseInt(parts[1].trim())) / 2.0;
                                        topic.setEstimatedHours((int) Math.ceil(avg));
                                    } else {
                                        topic.setEstimatedHours(Integer.parseInt(parts[0].trim()));
                                    }
                                } else {
                                    topic.setEstimatedHours(Integer.parseInt(hoursStr));
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("Could not parse hours from field '{}': {}", field, hoursNode);
                        // Continue to next possible field
                    }
                }
            }
            
            // Default to 2 hours if not specified
            if (topic.getEstimatedHours() == null || topic.getEstimatedHours() < 1) {
                topic.setEstimatedHours(2);
            }
            
            // Handle difficulty
            String[] possibleDiffFields = {"difficulty", "level", "complexity"};
            for (String field : possibleDiffFields) {
                if (topicNode.has(field) && topicNode.get(field).isTextual()) {
                    String diff = topicNode.get(field).asText().trim();
                    if (!diff.isEmpty()) {
                        // Normalize difficulty values
                        diff = diff.toLowerCase();
                        if (diff.startsWith("beginner") || diff.startsWith("easy")) {
                            topic.setDifficulty("Beginner");
                        } else if (diff.startsWith("intermediate") || diff.startsWith("medium")) {
                            topic.setDifficulty("Intermediate");
                        } else if (diff.startsWith("advanced") || diff.startsWith("hard")) {
                            topic.setDifficulty("Advanced");
                        } else {
                            // Capitalize first letter
                            topic.setDifficulty(Character.toUpperCase(diff.charAt(0)) + 
                                             (diff.length() > 1 ? diff.substring(1) : ""));
                        }
                        break;
                    }
                }
            }
            
            // Default to "Beginner" if not specified
            if (topic.getDifficulty() == null || topic.getDifficulty().isEmpty()) {
                topic.setDifficulty("Beginner");
            }
            
            // Handle subtopics
            String[] possibleSubtopicsFields = {"subtopics", "subTopics", "subsections", "details"};
            for (String field : possibleSubtopicsFields) {
                if (topicNode.has(field)) {
                    try {
                        JsonNode subtopicsNode = topicNode.get(field);
                        if (subtopicsNode.isArray() && subtopicsNode.size() > 0) {
                            List<com.pathprep.model.Subtopic> subtopics = new ArrayList<>();
                            for (JsonNode subtopicNode : subtopicsNode) {
                                try {
                                    com.pathprep.model.Subtopic subtopic = parseSubtopic(subtopicNode);
                                    if (subtopic != null) {
                                        subtopics.add(subtopic);
                                    }
                                } catch (Exception e) {
                                    log.warn("Failed to parse subtopic: {}", subtopicNode, e);
                                }
                            }
                            if (!subtopics.isEmpty()) {
                                topic.setSubtopics(subtopics);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error parsing subtopics from field '{}': {}", field, e.getMessage());
                    }
                }
            }
            
            return topic;
            
        } catch (Exception e) {
            log.error("Error parsing topic: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Parses a subtopic from a JSON node.
     */
private com.pathprep.model.Subtopic parseSubtopic(JsonNode subtopicNode) {
        if (subtopicNode == null || subtopicNode.isNull()) {
            return null;
        }
        
        try {
            com.pathprep.model.Subtopic subtopic = new com.pathprep.model.Subtopic();
            
            // Handle name (with multiple possible field names)
            String[] possibleNameFields = {"name", "title", "subtopicName", "concept"};
            for (String field : possibleNameFields) {
                if (subtopicNode.has(field) && subtopicNode.get(field).isTextual()) {
                    String name = subtopicNode.get(field).asText().trim();
                    if (!name.isEmpty()) {
                        subtopic.setName(name);
                        break;
                    }
                }
            }
            
            // Handle description
            String[] possibleDescFields = {"description", "desc", "details", "summary"};
            for (String field : possibleDescFields) {
                if (subtopicNode.has(field) && subtopicNode.get(field).isTextual()) {
                    String desc = subtopicNode.get(field).asText().trim();
                    if (!desc.isEmpty()) {
                        subtopic.setDescription(desc);
                        break;
                    }
                }
            }
            
            // If no name but has description, use first 50 chars of description as name
            if ((subtopic.getName() == null || subtopic.getName().isEmpty()) && 
                subtopic.getDescription() != null && !subtopic.getDescription().isEmpty()) {
                String desc = subtopic.getDescription();
                subtopic.setName(desc.substring(0, Math.min(50, desc.length())) + 
                               (desc.length() > 50 ? "..." : ""));
            }
            
            return subtopic;
            
        } catch (Exception e) {
            log.error("Error parsing subtopic: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Checks if a JSON node looks like a phase object.
     */
    private boolean isPhaseLike(JsonNode node) {
        if (!node.isObject()) return false;
        
        // A phase-like object should have at least one of these fields
        return node.has("phaseName") || 
               node.has("title") || 
               node.has("weekNumber") ||
               node.has("objective") ||
               node.has("topics");
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) 
            throws JsonMappingException {
        // Handle null property case by returning a new instance with null type
        if (property == null) {
            return new DetailedRoadmapDeserializer(null);
        }
        // If property has type information, use it
        JavaType type = property.getType();
        return new DetailedRoadmapDeserializer(type != null ? type.getRawClass() : null);
    }
}
