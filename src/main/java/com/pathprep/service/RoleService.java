package com.pathprep.service;

import com.pathprep.model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface RoleService {
    Flux<Role> findAllRoles();
    Optional<Role> findRoleById(String id);
    Role findRoleByName(String name);
    Role saveRole(Role role);
    Mono<Void> deleteRole(String id);
}
