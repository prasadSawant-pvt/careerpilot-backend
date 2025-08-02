package com.pathprep.service.impl;

import com.pathprep.model.Skill;
import com.pathprep.repository.SkillRepository;
import com.pathprep.service.SkillService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;

    public SkillServiceImpl(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Override
    public Flux<Skill> searchSkills(String query) {
        List<Skill> skills = skillRepository.findByNameContainingIgnoreCase(query);
        return Flux.fromIterable(skills);
    }

    @Override
    public Flux<Skill> findAllSkills() {
        List<Skill> skills = skillRepository.findAll();
        return Flux.fromIterable(skills);
    }

    @Override
    public Optional<Skill> findSkillById(String id) {
        return skillRepository.findById(id);
    }

    @Override
    public Skill findSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    @Override
    public Skill saveSkill(Skill skill) {
        // Check if skill with same name already exists
        Skill existingSkill = skillRepository.findByName(skill.getName());
        if (existingSkill != null) {
            return existingSkill; // Return existing skill instead of creating duplicate
        }
        return skillRepository.save(skill);
    }

    @Override
    public Mono<Void> deleteSkill(String id) {
        skillRepository.deleteById(id);
        return Mono.empty();
    }
}
