package com.poab.booking_system.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    boolean existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(UUID resource, Instant end, Instant start);
}
