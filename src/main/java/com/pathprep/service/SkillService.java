package com.pathprep.service;

import com.pathprep.model.Skill;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface SkillService {
    Flux<Skill> searchSkills(String query);
    Flux<Skill> findAllSkills();
    Optional<Skill> findSkillById(String id);
    Skill findSkillByName(String name);
    Skill saveSkill(Skill skill);
    Mono<Void> deleteSkill(String id);
}
