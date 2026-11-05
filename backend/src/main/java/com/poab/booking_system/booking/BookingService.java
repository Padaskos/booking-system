package com.poab.booking_system.booking;

import com.poab.booking_system.resource.InvalidRequestException;
import com.poab.booking_system.resource.ResourceService;
import com.poab.booking_system.resource.ResourceUnavailableException;
import com.poab.booking_system.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private final ResourceService resourceService;
    private final BookingRepository repository;

    BookingService(ResourceService resourceService, BookingRepository repository) {
        this.resourceService = resourceService;
        this.repository = repository;
    }

    public BookingEntity createBooking(UUID resourceId, UUID userId, Instant startTime, Instant endTime, String bookingDescription) {
        if (!resourceService.isActive(resourceId))
            throw new ResourceUnavailableException(resourceId);
        if (!startTime.isBefore(endTime) || startTime.isBefore(Instant.now()))
            throw new InvalidRequestException("Invalid time input");
        if (repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, endTime, startTime))
            throw new InvalidRequestException("Resource is in use during that time");

        BookingEntity entity = new BookingEntity(resourceId, userId, startTime, endTime, bookingDescription);
        repository.save(entity);
        return entity;
    }

    public boolean isTimeSlotAvailable(UUID resourceId, Instant start, Instant end) {
        if (!start.isBefore(end) || start.isBefore(Instant.now()) || !resourceService.isActive(resourceId))
            return false;

        return !repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, end, start);
    }

    public void deleteBooking(UUID bookingId, UUID userId, UserRole role) {
        BookingEntity booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (role != UserRole.ADMIN && !booking.getUserId().equals(userId))
            throw new UserNotAllowedException();

        repository.delete(booking);
    }
}
