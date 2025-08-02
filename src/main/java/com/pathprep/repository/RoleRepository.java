package com.pathprep.repository;

import com.pathprep.model.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends BaseRepository<Role, String> {
    // Custom queries can be added here

    Role findByName(String name);
}
