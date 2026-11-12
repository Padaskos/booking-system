package io.github.padaskos.bookingsystem.booking;

import io.github.padaskos.bookingsystem.booking.dto.BookingDetailsResponse;
import io.github.padaskos.bookingsystem.resource.InvalidRequestException;
import io.github.padaskos.bookingsystem.resource.ResourceNotFoundException;
import io.github.padaskos.bookingsystem.resource.ResourceService;
import io.github.padaskos.bookingsystem.resource.ResourceUnavailableException;
import io.github.padaskos.bookingsystem.resource.dto.ResourceNameResponse;
import io.github.padaskos.bookingsystem.user.UserRole;
import io.github.padaskos.bookingsystem.user.UserService;
import io.github.padaskos.bookingsystem.user.dto.UserEmailResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private Clock clock;

    @Test
    void bookingFailsWhenResourceIsInactive() {
        UUID resourceID = UUID.randomUUID();

        when(resourceService.isActive(resourceID)).thenReturn(false);

        assertThrows(ResourceUnavailableException.class, () -> bookingService.createBooking(
                resourceID,
                genericUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(600),
                "Booking"
        ));
    }

    @Test
    void bookingFailsWhenStartIsNotBeforeEnd() {
        when(resourceService.isActive(genericResourceId)).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> bookingService.createBooking(
                genericResourceId,
                genericUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(299),
                "Booking"
        ));
    }

    @Test
    void bookingFailsWhenStartIsInThePast() {
        when(resourceService.isActive(genericResourceId)).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> bookingService.createBooking(
                genericResourceId,
                genericUserId,
                nowPlusSeconds(-300),
                nowPlusSeconds(300),
                "Booking"
        ));
    }

    @Test
    void bookingFailsWhenResourceDoesNotExist() {
        UUID resourceId = UUID.randomUUID();
        when(resourceService.isActive(resourceId)).thenThrow(new ResourceNotFoundException(resourceId));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(
                resourceId,
                genericUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(600),
                "Booking"
        ));
    }

    @Test
    void bookingFailsWhenRequestedPeriodOverlapsExistingBooking() {
        Instant requestedStart = nowPlusSeconds(300);
        Instant requestedEnd = nowPlusSeconds(600);

        when(resourceService.isActive(genericResourceId)).thenReturn(true);
        when(bookingRepository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                genericResourceId, requestedEnd, requestedStart)).thenReturn(true);

        assertThrows(InvalidRequestException.class, () -> bookingService.createBooking(
                genericResourceId,
                genericUserId,
                requestedStart,
                requestedEnd,
                "Booking"
        ));
    }

    @Test
    void createBookingSavesConfirmedBookingWhenRequestIsValid() {
        Instant start = nowPlusSeconds(300);
        Instant end = nowPlusSeconds(600);

        when(resourceService.isActive(genericResourceId)).thenReturn(true);
        when(bookingRepository
                .existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                        genericResourceId, end, start
                ))
                .thenReturn(false);

        bookingService.createBooking(
                genericResourceId,
                genericUserId,
                start,
                end,
                "Booking"
        );

        ArgumentCaptor<BookingEntity> captor =
                ArgumentCaptor.forClass(BookingEntity.class);

        verify(bookingRepository).save(captor.capture());

        BookingEntity saved = captor.getValue();

        assertThat(saved.getResourceId()).isEqualTo(genericResourceId);
        assertThat(saved.getUserId()).isEqualTo(genericUserId);
        assertThat(saved.getStartTime()).isEqualTo(start);
        assertThat(saved.getEndTime()).isEqualTo(end);
        assertThat(saved.getBookingDescription()).isEqualTo("Booking");
    }

    @Test
    void timeSlotIsAvailableWhenRequestIsValid() {
        Instant start = nowPlusSeconds(300);
        Instant end = nowPlusSeconds(600);

        when(resourceService.isActive(genericResourceId)).thenReturn(true);
        when(bookingRepository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                genericResourceId, end, start
        )).thenReturn(false);

        boolean result = bookingService.isTimeSlotAvailable(genericResourceId, start, end);

        assertThat(result).isTrue();
    }

    @Test
    void timeSlotIsNotAvailableWhenBookingOverlaps() {
        Instant start = nowPlusSeconds(300);
        Instant end = nowPlusSeconds(600);

        when(resourceService.isActive(genericResourceId)).thenReturn(true);
        when(bookingRepository.existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
                genericResourceId, end, start
        )).thenReturn(true);

        boolean result = bookingService.isTimeSlotAvailable(genericResourceId, start, end);

        assertThat(result).isFalse();
    }

    @Test
    void timeSlotIsNotAvailableWhenResourceIsInactive() {
        Instant start = nowPlusSeconds(300);
        Instant end = nowPlusSeconds(600);

        when(resourceService.isActive(genericResourceId)).thenReturn(false);

        boolean result = bookingService.isTimeSlotAvailable(genericResourceId, start, end);

        assertThat(result).isFalse();
    }

    @Test
    void timeSlotIsNotAvailableWhenTimeInputIsInvalid() {
        Instant start = nowPlusSeconds(600);
        Instant end = nowPlusSeconds(300);

        boolean result = bookingService.isTimeSlotAvailable(genericResourceId, start, end);

        assertThat(result).isFalse();
    }

    @Test
    void deleteBookingDeletesBookingWhenUserOwnsBooking() {
        UUID bookingId = UUID.randomUUID();
        BookingEntity booking = new BookingEntity(
                genericResourceId,
                genericUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(600),
                "Booking"
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(bookingId, genericUserId, UserRole.USER);

        verify(bookingRepository).delete(booking);
    }

    @Test
    void getMyBookingsReturnsUpcomingBookingsWithResourceNames() {
        UUID resourceId = UUID.randomUUID();
        BookingEntity booking = booking(resourceId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "Team meeting");

        givenCurrentTime();
        givenUpcomingBookings(PageRequest.of(0, 20), booking);
        givenResourceNames(new ResourceNameResponse(resourceId, "Meeting Room A"));

        Page<BookingDetailsResponse> result = bookingService.getMyBookings(
                genericUserId,
                BookingPeriod.UPCOMING,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertBookingResponse(result.getContent().getFirst(), resourceId, "Meeting Room A", "Team meeting");

        verify(bookingRepository).findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
                genericUserId,
                fixedNow(),
                PageRequest.of(0, 20)
        );
        verify(bookingRepository, never()).findAllByUserIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                any(),
                any(),
                any()
        );
    }

    @Test
    void getMyBookingsReturnsPreviousBookingsWithResourceNames() {
        UUID resourceId = UUID.randomUUID();
        BookingEntity booking = booking(resourceId, "2026-10-29T11:00:00Z", "2026-10-29T12:00:00Z", "Old meeting");

        givenCurrentTime();
        givenPreviousBookings(PageRequest.of(0, 20), booking);
        givenResourceNames(new ResourceNameResponse(resourceId, "Meeting Room A"));

        Page<BookingDetailsResponse> result = bookingService.getMyBookings(
                genericUserId,
                BookingPeriod.PREVIOUS,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertBookingResponse(result.getContent().getFirst(), resourceId, "Meeting Room A", "Old meeting");

        verify(bookingRepository).findAllByUserIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                genericUserId,
                fixedNow(),
                PageRequest.of(0, 20)
        );
        verify(bookingRepository, never()).findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
                any(),
                any(),
                any()
        );
    }

    @Test
    void getMyBookingsRequestsResourceNamesOnlyOnceForPage() {
        UUID firstResourceId = UUID.randomUUID();
        UUID secondResourceId = UUID.randomUUID();

        givenCurrentTime();
        givenUpcomingBookings(
                PageRequest.of(0, 20),
                booking(firstResourceId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "First booking"),
                booking(secondResourceId, "2026-10-30T13:00:00Z", "2026-10-30T14:00:00Z", "Second booking")
        );
        givenResourceNames(
                new ResourceNameResponse(firstResourceId, "Laptop A"),
                new ResourceNameResponse(secondResourceId, "Camera A")
        );

        Page<BookingDetailsResponse> result = bookingService.getMyBookings(
                genericUserId,
                BookingPeriod.UPCOMING,
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).itemName()).isEqualTo("Laptop A");
        assertThat(result.getContent().get(1).itemName()).isEqualTo("Camera A");

        verify(resourceService, times(1)).getRequestedResourceNamesById(List.of(firstResourceId, secondResourceId));
    }

    @Test
    void getMyBookingsRequestsEachResourceNameOnlyOnceWhenResourceIsUsedMultipleTimes() {
        UUID resourceId = UUID.randomUUID();

        givenCurrentTime();
        givenUpcomingBookings(
                PageRequest.of(0, 20),
                booking(resourceId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "First booking"),
                booking(resourceId, "2026-10-30T13:00:00Z", "2026-10-30T14:00:00Z", "Second booking")
        );
        givenResourceNames(new ResourceNameResponse(resourceId, "Meeting Room A"));

        Page<BookingDetailsResponse> result = bookingService.getMyBookings(
                genericUserId,
                BookingPeriod.UPCOMING,
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).itemName()).isEqualTo("Meeting Room A");
        assertThat(result.getContent().get(1).itemName()).isEqualTo("Meeting Room A");

        verify(resourceService, times(1)).getRequestedResourceNamesById(List.of(resourceId));
    }

    @Test
    void getMyBookingsKeepsPageMetadata() {
        UUID resourceId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 2);

        BookingEntity booking = booking(resourceId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "Booking");

        givenCurrentTime();
        when(bookingRepository.findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
                genericUserId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(booking), pageable, 5));
        givenResourceNames(new ResourceNameResponse(resourceId, "Meeting Room A"));

        Page<BookingDetailsResponse> result = bookingService.getMyBookings(
                genericUserId,
                BookingPeriod.UPCOMING,
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void getResourceBookingsReturnsUpcomingBookingsWithUserEmails() {
        UUID userId = UUID.randomUUID();
        BookingEntity booking = bookingForUser(userId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "Team meeting");

        givenCurrentTime();
        givenUpcomingResourceBookings(PageRequest.of(0, 20), booking);
        givenUserEmails(new UserEmailResponse(userId, "user@example.com"));

        Page<BookingDetailsResponse> result = bookingService.getResourceBookings(
                genericResourceId,
                BookingPeriod.UPCOMING,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertBookingResponse(result.getContent().getFirst(), userId, "user@example.com", "Team meeting");

        verify(bookingRepository).findAllByResourceIdAndEndTimeAfterOrderByStartTimeAsc(
                genericResourceId,
                fixedNow(),
                PageRequest.of(0, 20)
        );
        verify(bookingRepository, never()).findAllByResourceIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                any(),
                any(),
                any()
        );
    }

    @Test
    void getResourceBookingsReturnsPreviousBookingsWithUserEmails() {
        UUID userId = UUID.randomUUID();
        BookingEntity booking = bookingForUser(userId, "2026-10-29T11:00:00Z", "2026-10-29T12:00:00Z", "Old meeting");

        givenCurrentTime();
        givenPreviousResourceBookings(PageRequest.of(0, 20), booking);
        givenUserEmails(new UserEmailResponse(userId, "user@example.com"));

        Page<BookingDetailsResponse> result = bookingService.getResourceBookings(
                genericResourceId,
                BookingPeriod.PREVIOUS,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertBookingResponse(result.getContent().getFirst(), userId, "user@example.com", "Old meeting");

        verify(bookingRepository).findAllByResourceIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                genericResourceId,
                fixedNow(),
                PageRequest.of(0, 20)
        );
        verify(bookingRepository, never()).findAllByResourceIdAndEndTimeAfterOrderByStartTimeAsc(
                any(),
                any(),
                any()
        );
    }

    @Test
    void getResourceBookingsRequestsEachUserEmailOnlyOnceWhenUserHasMultipleBookings() {
        UUID userId = UUID.randomUUID();

        givenCurrentTime();
        givenUpcomingResourceBookings(
                PageRequest.of(0, 20),
                bookingForUser(userId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "First booking"),
                bookingForUser(userId, "2026-10-30T13:00:00Z", "2026-10-30T14:00:00Z", "Second booking")
        );
        givenUserEmails(new UserEmailResponse(userId, "user@example.com"));

        Page<BookingDetailsResponse> result = bookingService.getResourceBookings(
                genericResourceId,
                BookingPeriod.UPCOMING,
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).itemName()).isEqualTo("user@example.com");
        assertThat(result.getContent().get(1).itemName()).isEqualTo("user@example.com");

        verify(userService, times(1)).getUserEmailById(List.of(userId));
    }

    @Test
    void getResourceBookingsKeepsPageMetadata() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 2);

        BookingEntity booking = bookingForUser(userId, "2026-10-30T11:00:00Z", "2026-10-30T12:00:00Z", "Booking");

        givenCurrentTime();
        when(bookingRepository.findAllByResourceIdAndEndTimeAfterOrderByStartTimeAsc(
                genericResourceId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(booking), pageable, 5));
        givenUserEmails(new UserEmailResponse(userId, "user@example.com"));

        Page<BookingDetailsResponse> result = bookingService.getResourceBookings(
                genericResourceId,
                BookingPeriod.UPCOMING,
                pageable
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void deleteBookingDeletesBookingWhenUserIsAdmin() {
        UUID bookingId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        BookingEntity booking = new BookingEntity(
                genericResourceId,
                anotherUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(600),
                "Booking"
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(bookingId, genericUserId, UserRole.ADMIN);

        verify(bookingRepository).delete(booking);
    }

    @Test
    void deleteBookingFailsWhenBookingDoesNotExist() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.deleteBooking(
                bookingId,
                genericUserId,
                UserRole.USER
        ));
    }

    @Test
    void deleteBookingFailsWhenUserIsNotAdminAndDoesNotOwnBooking() {
        UUID bookingId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        BookingEntity booking = new BookingEntity(
                genericResourceId,
                anotherUserId,
                nowPlusSeconds(300),
                nowPlusSeconds(600),
                "Booking"
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(UserNotAllowedException.class, () -> bookingService.deleteBooking(
                bookingId,
                genericUserId,
                UserRole.USER
        ));

        verify(bookingRepository, never()).delete(booking);
    }

    private static final UUID genericResourceId = UUID.randomUUID();
    private static final UUID genericUserId = UUID.randomUUID();

    private static Instant nowPlusSeconds(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    private void givenCurrentTime() {
        when(clock.instant()).thenReturn(fixedNow());
    }

    private void givenUpcomingBookings(Pageable pageable, BookingEntity... bookings) {
        when(bookingRepository.findAllByUserIdAndEndTimeAfterOrderByStartTimeAsc(
                genericUserId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(bookings), pageable, bookings.length));
    }

    private void givenPreviousBookings(Pageable pageable, BookingEntity... bookings) {
        when(bookingRepository.findAllByUserIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                genericUserId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(bookings), pageable, bookings.length));
    }

    private void givenUpcomingResourceBookings(Pageable pageable, BookingEntity... bookings) {
        when(bookingRepository.findAllByResourceIdAndEndTimeAfterOrderByStartTimeAsc(
                genericResourceId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(bookings), pageable, bookings.length));
    }

    private void givenPreviousResourceBookings(Pageable pageable, BookingEntity... bookings) {
        when(bookingRepository.findAllByResourceIdAndEndTimeLessThanEqualOrderByEndTimeDesc(
                genericResourceId,
                fixedNow(),
                pageable
        )).thenReturn(new PageImpl<>(List.of(bookings), pageable, bookings.length));
    }

    private void givenResourceNames(ResourceNameResponse... resources) {
        when(resourceService.getRequestedResourceNamesById(
                Stream.of(resources)
                        .map(ResourceNameResponse::id)
                        .distinct()
                        .toList()
        )).thenReturn(List.of(resources));
    }

    private void givenUserEmails(UserEmailResponse... users) {
        when(userService.getUserEmailById(
                Stream.of(users)
                        .map(UserEmailResponse::id)
                        .distinct()
                        .toList()
        )).thenReturn(List.of(users));
    }

    private static BookingEntity booking(
            UUID itemId,
            String startTime,
            String endTime,
            String description
    ) {
        return new BookingEntity(
                itemId,
                genericUserId,
                Instant.parse(startTime),
                Instant.parse(endTime),
                description
        );
    }

    private static BookingEntity bookingForUser(
            UUID itemId,
            String startTime,
            String endTime,
            String description
    ) {
        return new BookingEntity(
                genericResourceId,
                itemId,
                Instant.parse(startTime),
                Instant.parse(endTime),
                description
        );
    }

    private static void assertBookingResponse(
            BookingDetailsResponse response,
            UUID itemId,
            String itemName,
            String description
    ) {
        assertThat(response.itemId()).isEqualTo(itemId);
        assertThat(response.itemName()).isEqualTo(itemName);
        assertThat(response.bookingDescription()).isEqualTo(description);
    }

    private static Instant fixedNow() {
        return Instant.parse("2026-10-30T10:00:00Z");
    }
}
