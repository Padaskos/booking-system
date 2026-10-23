package com.poab.booking_system.resource.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull Boolean status) {
}
