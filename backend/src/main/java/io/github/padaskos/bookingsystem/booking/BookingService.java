package io.github.padaskos.bookingsystem.booking;

import io.github.padaskos.bookingsystem.booking.dto.ResourceBookingDetailsResponse;
import io.github.padaskos.bookingsystem.booking.dto.UserBookingDetailsResponse;
import io.github.padaskos.bookingsystem.resource.InvalidRequestException;
import io.github.padaskos.bookingsystem.resource.ResourceService;
import io.github.padaskos.bookingsystem.resource.dto.ResourceNameResponse;
import io.github.padaskos.bookingsystem.user.UserRole;
import io.github.padaskos.bookingsystem.user.UserService;
import io.github.padaskos.bookingsystem.user.dto.UserEmailResponse;
import jakarta.transaction.Transactional;
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
    private final UserService userService;

    BookingService(ResourceService resourceService, BookingRepository repository, Clock clock, UserService userService) {
        this.resourceService = resourceService;
        this.repository = repository;
        this.clock = clock;
        this.userService = userService;
    }

    @Transactional
    public void createBooking(UUID resourceId, UUID userId, Instant startTime, Instant endTime, String bookingDescription) {
        resourceService.lockAndVerifyActive(resourceId);

        if (!startTime.isBefore(endTime) || startTime.isBefore(Instant.now(clock)))
            throw new InvalidRequestException("Invalid time input");
        if (repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, endTime, startTime))
            throw new InvalidRequestException("Resource is in use during that time");

        repository.saveAndFlush(new BookingEntity(resourceId, userId, startTime, endTime, bookingDescription));
    }

    public boolean isTimeSlotAvailable(UUID resourceId, Instant start, Instant end) {
        if (!resourceService.isActive(resourceId))
            return false;

        if (!start.isBefore(end) || start.isBefore(Instant.now(clock)))
            return false;

        return !repository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(resourceId, end, start);
    }

    public Page<UserBookingDetailsResponse> getMyBookings(UUID userId, BookingPeriod period, Pageable pageable) {
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

    private Page<UserBookingDetailsResponse> enrichWithResourceNames(Page<BookingEntity> bookings) {
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

        return bookings.map(booking -> new UserBookingDetailsResponse(
                booking.getId(),
                booking.getResourceId(),
                resourceNamesById.get(booking.getResourceId()),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDescription()
        ));
    }

    public Page<ResourceBookingDetailsResponse> getResourceBookings(UUID resourceId, BookingPeriod period, Pageable pageable) {
        Instant currentTime = Instant.now(clock);

        Page<BookingEntity> bookings = switch (period) {
            case UPCOMING -> repository.findAllByResourceIdAndEndTimeAfterOrderByStartTimeAsc(
                    resourceId,
                    currentTime,
                    pageable
            );
            case PREVIOUS -> repository.findAllByResourceIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                    resourceId,
                    currentTime,
                    pageable
            );
        };

        return enrichWithUserEmails(bookings);
    }

    private Page<ResourceBookingDetailsResponse> enrichWithUserEmails(Page<BookingEntity> bookings) {
        List<UUID> userIds = bookings.getContent()
                .stream()
                .map(BookingEntity::getUserId)
                .distinct()
                .toList();

        Map<UUID, String> userEmailsById = userService.getUserEmailById(userIds)
                .stream().collect(Collectors.toMap(
                        UserEmailResponse::id,
                        UserEmailResponse::email
                ));

        return bookings.map(booking -> new ResourceBookingDetailsResponse(
                booking.getId(),
                booking.getUserId(),
                userEmailsById.get(booking.getUserId()),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDescription()
        ));
    }

    public void cancelBooking(UUID bookingId, UUID userId, UserRole role) {
        BookingEntity booking = repository.findById(bookingId)
                .orElse(null);
        if (booking == null)
            return;
        if (role != UserRole.ADMIN && !booking.getUserId().equals(userId))
            throw new UserNotAllowedException();
        Instant currentTime = Instant.now(clock);
        if (!booking.getEndTime().isAfter(currentTime))
            throw new InvalidRequestException("One can't cancel a past booking");
        repository.delete(booking);
    }
}
