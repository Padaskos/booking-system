package com.poab.booking_system.resource.dto;

import com.poab.booking_system.resource.ResourceType;

public record ResourceResponse(
    String name,
    String description,
    String location,
    ResourceType type
) {
}
