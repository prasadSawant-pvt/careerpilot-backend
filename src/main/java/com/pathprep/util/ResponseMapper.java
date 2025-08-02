package com.pathprep.util;

import com.pathprep.model.InterviewQuestion;
import com.pathprep.model.Roadmap;
import com.pathprep.model.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseMapper {
    
    private static final Pattern SECTION_PATTERN = Pattern.compile("##\\s*(.+?)\\s*([^#]*)(?=##|$)", Pattern.DOTALL);
    private static final Pattern LIST_ITEM_PATTERN = Pattern.compile("-\\s*(.+?)(?=\\n\\s*-|$)", Pattern.DOTALL);
    
    public static Roadmap mapToRoadmap(String aiResponse, String role, String experience, List<String> skills) {
        Roadmap roadmap = new Roadmap();
        roadmap.setRole(role);
        roadmap.setExperience(experience);
        roadmap.setSkills(skills);
        
        List<Section> sections = new ArrayList<>();
        Matcher sectionMatcher = SECTION_PATTERN.matcher(aiResponse);
        
        while (sectionMatcher.find()) {
            String title = sectionMatcher.group(1).trim();
            String content = sectionMatcher.group(2).trim();
            
            Section section = new Section();
            section.setTitle(title);
            
            // Extract list items from section content
            List<String> items = new ArrayList<>();
            Matcher itemMatcher = LIST_ITEM_PATTERN.matcher(content);
            while (itemMatcher.find()) {
                items.add(itemMatcher.group(1).trim());
            }
            section.setItems(items);
            
            sections.add(section);
        }
        
        roadmap.setSections(sections);
        return roadmap;
    }
    
    public static List<InterviewQuestion> mapToInterviewQuestions(String aiResponse) {
        List<InterviewQuestion> questions = new ArrayList<>();
        // This is a simplified version - in a real app, you'd need more sophisticated parsing
        // based on the exact format of the AI response
        
        String[] questionBlocks = aiResponse.split("\\n\\s*\\n");
        
        for (String block : questionBlocks) {
            if (block.trim().isEmpty()) continue;
            
            InterviewQuestion question = new InterviewQuestion();
            String[] lines = block.split("\\n");
            
            if (lines.length > 0) {
                question.setQuestion(lines[0].replaceFirst("^\\d+\\.\\s*", ""));
                
                // Simple parsing - in a real app, you'd want more robust parsing
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (line.toLowerCase().startsWith("answer:")) {
                        question.setAnswer(line.substring("answer:".length()).trim());
                    } else if (line.toLowerCase().startsWith("difficulty:")) {
                        question.setDifficulty(line.substring("difficulty:".length()).trim());
                    }
                }
                
                questions.add(question);
            }
        }
        
        return questions;
    }
}
