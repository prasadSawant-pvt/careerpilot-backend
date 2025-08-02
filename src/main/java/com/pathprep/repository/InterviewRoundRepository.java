package com.pathprep.repository;

import com.pathprep.model.InterviewRound;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRoundRepository extends BaseRepository<InterviewRound, String> {
    @Query("{ 'role': ?0, 'experience': ?1 }")
    List<InterviewRound> findByRoleAndExperience(String role, String experience);
    
    @Query("{ 'role': ?0, 'experience': ?1, 'name': ?2 }")
    InterviewRound findByRoleAndExperienceAndName(String role, String experience, String name);
}
