package com.pathprep.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Document("interviewRounds")
public class InterviewRound extends BaseEntity {
    @Indexed
    private String role;
    @Indexed
    private String experience;
    private String name;
    private String description;
    private int duration; // in minutes
    
    @DocumentReference
    private List<InterviewQuestion> questions;
}
