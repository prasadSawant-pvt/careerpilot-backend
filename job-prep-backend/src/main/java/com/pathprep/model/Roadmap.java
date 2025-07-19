package com.pathprep.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Document("roadmaps")
@CompoundIndex(def = "{'role': 1, 'experience': 1}", unique = true)
public class Roadmap extends BaseEntity {
    @Indexed
    private String role;
    @Indexed
    private String experience;
    private List<String> skills;
    private String timeline;
    private List<Section> sections;
    private int popularity;

    private String experienceLevel;
    
    @DocumentReference
    private List<InterviewQuestion> commonQuestions;

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public List<InterviewQuestion> getCommonQuestions() {
        return commonQuestions;
    }

    public void setCommonQuestions(List<InterviewQuestion> commonQuestions) {
        this.commonQuestions = commonQuestions;
    }
}
