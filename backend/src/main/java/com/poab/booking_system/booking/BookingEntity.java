package com.poab.booking_system.booking;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "booking_description")
    private String bookingDescription;

    public BookingEntity() {}

    public BookingEntity(UUID resourceId, UUID userId, Instant startTime, Instant endTime, String bookingDescription) {
        this.resourceId = resourceId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bookingDescription = bookingDescription;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getBookingDescription() {
        return bookingDescription;
    }
}

