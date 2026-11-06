package io.github.padaskos.bookingsystem.resource.dto;

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
    ) {}
