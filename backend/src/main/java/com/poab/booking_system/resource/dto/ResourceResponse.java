package com.poab.booking_system.resource.dto;

import com.poab.booking_system.resource.ResourceType;

import java.util.UUID;

public record ResourceResponse(
        UUID Id,
        String name,
        String description,
        String location,
        ResourceType type
) {
}
