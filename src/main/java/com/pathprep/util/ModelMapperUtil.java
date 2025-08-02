package com.pathprep.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

/**
 * Utility class for object mapping using ModelMapper.
 * Provides methods for mapping between DTOs and entities.
 */
@Component
public class ModelMapperUtil extends ModelMapper {
    
    public ModelMapperUtil() {
        super();
        this.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setSkipNullEnabled(true);
    }
    
    /**
     * Maps the source object to the destination class.
     * 
     * @param <D> The type of the destination object
     * @param source The source object to map from
     * @param destinationType The destination type to map to
     * @return A new instance of the destination type with values from source
     */
    @Override
    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        return super.map(source, destinationType);
    }
}
