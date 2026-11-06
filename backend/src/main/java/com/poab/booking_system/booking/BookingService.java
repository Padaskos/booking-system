package com.poab.booking_system.booking;

import com.poab.booking_system.booking.dto.BookingDetailsResponse;
import com.poab.booking_system.resource.InvalidRequestException;
import com.poab.booking_system.resource.ResourceService;
import com.poab.booking_system.resource.ResourceUnavailableException;
import com.poab.booking_system.resource.dto.ResourceNameResponse;
import com.poab.booking_system.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final ResourceService resourceService;
    private final BookingRepository repository;
    private final Clock clock;

    BookingService(ResourceService resourceService, BookingRepository repository, Clock clock) {
        this.resourceService = resourceService;
        this.repository = repository;
        this.clock = clock;
    }

    public void createBooking(UUID resourceId, UUID userId, Instant startTime, Instant endTime, String bookingDescription) {
        if (!resourceService.isActive(resourceId))
            throw new ResourceUnavailableException(resourceId);
        if (!startTime.isBefore(endTime) || startTime.isBefore(Instant.now()))
            throw new InvalidRequestException("Invalid time input");
        if (repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, endTime, startTime))
            throw new InvalidRequestException("Resource is in use during that time");

        repository.save(new BookingEntity(resourceId, userId, startTime, endTime, bookingDescription));
    }

    public boolean isTimeSlotAvailable(UUID resourceId, Instant start, Instant end) {
        if (!start.isBefore(end) || start.isBefore(Instant.now()) || !resourceService.isActive(resourceId))
            return false;

        return !repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, end, start);
    }

    public Page<BookingDetailsResponse> getMyBookings(UUID userId, BookingPeriod period, Pageable pageable) {
        Instant currentTime = Instant.now(clock);

        Page<BookingEntity> bookings = switch (period) {
            case UPCOMING -> repository.findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
                    userId,
                    currentTime,
                    pageable
            );
            case PREVIOUS -> repository.findAllByUserIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                    userId,
                    currentTime,
                    pageable
            );
        };

        return enrichWithResourceNames(bookings);
    }

    private Page<BookingDetailsResponse> enrichWithResourceNames(Page<BookingEntity> bookings) {
        List<UUID> resourceIds = bookings.getContent()
                .stream()
                .map(BookingEntity::getResourceId)
                .distinct()
                .toList();

        Map<UUID, String> resourceNamesById = resourceService.getRequestedResourceNamesById(resourceIds)
                .stream().collect(Collectors.toMap(
                        ResourceNameResponse::id,
                        ResourceNameResponse::name
                ));

        return bookings.map(booking -> new BookingDetailsResponse(
                booking.getId(),
                booking.getResourceId(),
                resourceNamesById.get(booking.getResourceId()),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDescription()
        ));
    }

    public void deleteBooking(UUID bookingId, UUID userId, UserRole role) {
        BookingEntity booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (role != UserRole.ADMIN && !booking.getUserId().equals(userId))
            throw new UserNotAllowedException();

        repository.delete(booking);
    }
}
