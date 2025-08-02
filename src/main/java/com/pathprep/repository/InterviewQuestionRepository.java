package com.pathprep.repository;

import com.pathprep.model.InterviewQuestion;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepository extends BaseRepository<InterviewQuestion, String> {
    List<InterviewQuestion> findByRoleAndExperience(String role, String experience);
    
    @Query("{ 'role': ?0, 'experience': ?1, 'skill': ?2 }")
    List<InterviewQuestion> findByRoleAndExperienceAndSkill(String role, String experience, String skill);
    
    List<InterviewQuestion> findByDifficulty(String difficulty);
    
    @Query("{ 'tags': { $in: ?0 } }")
    List<InterviewQuestion> findByTags(List<String> tags);
}
