package com.pathprep.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document("skills")
public class Skill extends BaseEntity {
    @Indexed(unique = true)
    private String name;
    private String category;
    private Integer proficiency;
    private Boolean isCore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getProficiency() {
        return proficiency;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public Boolean getCore() {
        return isCore;
    }

    public void setCore(Boolean core) {
        isCore = core;
    }
}
