package com.poab.booking_system.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingResponse(
        UUID bookingId,
        UUID resourceId,
        UUID userId,
        Instant startTime,
        Instant endTime,
        String bookingDescription
) {}
