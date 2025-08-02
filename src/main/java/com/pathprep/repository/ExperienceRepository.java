package com.pathprep.repository;

import com.pathprep.model.Experience;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperienceRepository extends BaseRepository<Experience, String> {
    // Custom queries can be added here
}
