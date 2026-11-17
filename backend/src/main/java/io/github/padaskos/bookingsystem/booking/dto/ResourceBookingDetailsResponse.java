package io.github.padaskos.bookingsystem.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record ResourceBookingDetailsResponse(
        UUID bookingId,
        UUID userId,
        String userEmail,
        Instant startTime,
        Instant endTime,
        String bookingDescription
) {}