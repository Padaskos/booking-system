package io.github.padaskos.bookingsystem.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID resourceId,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @NotBlank @Size(max = 500) String bookingDescription
) {
}
