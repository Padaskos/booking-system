package com.poab.booking_system.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingDetailsResponse(
        UUID bookingId,
        UUID resourceId,
        String resourceName,
        Instant startTime,
        Instant endTime,
        String bookingDescription
) {}
