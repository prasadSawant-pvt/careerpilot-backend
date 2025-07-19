package com.pathprep.service.impl;

import com.pathprep.model.Role;
import com.pathprep.repository.RoleRepository;
import com.pathprep.service.RoleService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Flux<Role> findAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return Flux.fromIterable(roles);
    }

    @Override
    public Optional<Role> findRoleById(String id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Role saveRole(Role role) {
        // Check if role with same name already exists
        Role existingRole = roleRepository.findByName(role.getName());
        if (existingRole != null) {
            return existingRole; // Return existing role instead of creating duplicate
        }
        return roleRepository.save(role);
    }

    @Override
    public Mono<Void> deleteRole(String id) {
        roleRepository.deleteById(id);
        return Mono.empty();
    }
}
