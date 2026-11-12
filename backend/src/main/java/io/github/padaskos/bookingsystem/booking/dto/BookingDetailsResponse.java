package io.github.padaskos.bookingsystem.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingDetailsResponse(
        UUID bookingId,
        UUID itemId,
        String itemName,
        Instant startTime,
        Instant endTime,
        String bookingDescription
) {}
