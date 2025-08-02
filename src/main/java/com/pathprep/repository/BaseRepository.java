package com.pathprep.repository;

import com.pathprep.model.BaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, ID extends Serializable> 
        extends MongoRepository<T, ID> {
    // Common repository methods can be defined here
}
