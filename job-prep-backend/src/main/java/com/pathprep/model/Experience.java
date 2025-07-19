package com.pathprep.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = false)
@Document("experiences")
public class Experience extends BaseEntity {
    private String name;
    private String description;
}
