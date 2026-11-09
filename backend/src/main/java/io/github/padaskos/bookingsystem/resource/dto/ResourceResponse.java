package io.github.padaskos.bookingsystem.resource.dto;

import io.github.padaskos.bookingsystem.resource.ResourceEntity;
import io.github.padaskos.bookingsystem.resource.ResourceType;

import java.util.UUID;

public record ResourceResponse(
        UUID Id,
        String name,
        String description,
        String location,
        ResourceType type
) {
    public static ResourceResponse fromEntity(ResourceEntity entity) {
        return new ResourceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getType()
        );
    }
}
