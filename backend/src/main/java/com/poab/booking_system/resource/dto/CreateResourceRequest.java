package com.poab.booking_system.resource.dto;

import com.poab.booking_system.resource.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateResourceRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        @Size(max = 100)
        String location,

        @NotNull
        ResourceType type
) {}
