package com.poab.booking_system.booking;

import com.poab.booking_system.resource.InvalidRequestException;
import com.poab.booking_system.resource.ResourceNotFoundException;
import com.poab.booking_system.resource.ResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;


    @Test
    void bookingFailsWhenResourceIsInactive() {
        UUID resourceID = UUID.randomUUID();

        when(resourceService.isActive(resourceID)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(
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


    private static final UUID genericResourceId = UUID.randomUUID();
    private static final UUID genericUserId = UUID.randomUUID();

    private static Instant nowPlusSeconds(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }


}

