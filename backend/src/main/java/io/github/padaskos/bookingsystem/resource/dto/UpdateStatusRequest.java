package io.github.padaskos.bookingsystem.resource.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull Boolean status) {
}
