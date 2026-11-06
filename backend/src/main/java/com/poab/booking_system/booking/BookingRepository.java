package com.poab.booking_system.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    boolean existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(UUID resource, Instant end, Instant start);

    Page<BookingEntity> findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
            UUID userId,
            Instant currentTime,
            Pageable pageable
    );

    Page<BookingEntity> findAllByUserIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
            UUID userId,
            Instant currentTime,
            Pageable pageable
    );
}
