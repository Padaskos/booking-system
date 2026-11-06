package io.github.padaskos.bookingsystem.resource.dto;

import io.github.padaskos.bookingsystem.resource.ResourceType;

import java.util.UUID;

public record ResourceResponse(
        UUID Id,
        String name,
        String description,
        String location,
        ResourceType type
) {
}
