package com.poab.booking_system.booking;

import com.poab.booking_system.resource.InvalidRequestException;
import com.poab.booking_system.resource.ResourceNotFoundException;
import com.poab.booking_system.resource.ResourceService;
import com.poab.booking_system.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private final ResourceService resourceService;
    private final BookingRepository repository;
    private final UserService userService;

    BookingService(ResourceService resourceService, BookingRepository repository, UserService userService) {
        this.resourceService = resourceService;
        this.repository = repository;
        this.userService = userService;
    }

    public void createBooking(UUID resourceId, UUID userId, Instant start, Instant end, String bookingDescription) {
        if (!resourceService.isActive(resourceId))
            throw new ResourceNotFoundException(resourceId);
        if (!start.isBefore(end) || start.isBefore(Instant.now()))
            throw new InvalidRequestException("Invalid time input");
        if (repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, end, start))
            throw new InvalidRequestException("Resource is in use during that time");

        repository.save(new BookingEntity(resourceId, userId, start, end, bookingDescription));
    }

    public boolean isTimeSlotAvailable(UUID resourceId, Instant start, Instant end) {
        if ((!start.isBefore(end) && start.isBefore(Instant.now())) || !resourceService.isActive(resourceId))
            return false;

        return !repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, end, start);
    }

    public void deleteBooking(UUID bookingId, UUID userId) {
        BookingEntity booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (!userService.isAdmin(userId) && !booking.getUserId().equals(userId))
            throw new UserNotAllowedException();
        repository.delete(booking);
    }
}
