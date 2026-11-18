package io.github.padaskos.bookingsystem.resource.dto;

import io.github.padaskos.bookingsystem.resource.ResourceEntity;
import io.github.padaskos.bookingsystem.resource.ResourceType;

import java.time.Instant;
import java.util.UUID;

public record DetailedResourceResponse(
        UUID id,
        String name,
        String description,
        String location,
        ResourceType type,
        boolean isActive,
        Instant creationTimestamp,
        Instant updatedTimestamp
    ) {
    public static DetailedResourceResponse fromEntity(ResourceEntity entity) {
        return new DetailedResourceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getType(),
                entity.isActive(),
                entity.getCreationTimestamp(),
                entity.getUpdatedTimestamp()
        );
    }
}
