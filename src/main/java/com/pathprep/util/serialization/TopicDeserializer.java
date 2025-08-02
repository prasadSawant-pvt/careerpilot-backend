package com.pathprep.util.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.pathprep.model.Topic;
import java.io.IOException;

public class TopicDeserializer extends JsonDeserializer<Topic> {
    @Override
    public Topic deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Topic topic = new Topic();
        
        if (node.isTextual()) {
            // Handle case where topic is just a string
            topic.setTopicName(node.asText());
            topic.setDescription("");
            topic.setEstimatedHours(0);
            topic.setDifficulty("Beginner");
        } else if (node.isObject()) {
            // Handle case where topic is a full object
            if (node.has("topicName")) {
                topic.setTopicName(node.get("topicName").asText());
            }
            if (node.has("description")) {
                topic.setDescription(node.get("description").asText());
            }
            if (node.has("estimatedHours")) {
                topic.setEstimatedHours(node.get("estimatedHours").asInt());
            }
            if (node.has("difficulty")) {
                topic.setDifficulty(node.get("difficulty").asText());
            }
            // Other fields can be added here if needed
        }
        
        return topic;
    }
}
