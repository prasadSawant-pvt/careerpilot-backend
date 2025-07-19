package com.pathprep.repository;

import com.pathprep.model.Skill;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends BaseRepository<Skill, String> {
    List<Skill> findByNameContainingIgnoreCase(String name);
    
    @Query("{ 'category': ?0 }")
    List<Skill> findByCategory(String category);
    
    List<Skill> findByIsCore(boolean isCore);

    Skill findByName(String name);

}
