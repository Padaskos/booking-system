package com.poab.booking_system.booking;

import com.poab.booking_system.auth.AuthService;
import com.poab.booking_system.auth.dto.UserDetailsResponse;
import com.poab.booking_system.booking.dto.AvailabilityResponse;
import com.poab.booking_system.booking.dto.CreateBookingRequest;
import com.poab.booking_system.booking.dto.CreateBookingResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService service;
    private final AuthService authService;

    public BookingController(BookingService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateBookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request, Authentication authentication) {
        UserDetailsResponse user = authService.getUserDetailsByEmail(authentication.getName());
        BookingEntity booking = service.createBooking(
                request.resourceId(),
                user.id(),
                request.startTime(),
                request.endTime(),
                request.bookingDescription()
        );

        return new CreateBookingResponse(
                booking.getId(),
                booking.getResourceId(),
                booking.getUserId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getBookingDescription()
        );
    }

    @GetMapping("/availability/{resourceId}")
    public AvailabilityResponse isTimeSlotAvailable(
            @PathVariable UUID resourceId,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime
            ) {
        return new AvailabilityResponse(service.isTimeSlotAvailable(resourceId, startTime, endTime));
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable UUID bookingId, Authentication authentication) {
        UserDetailsResponse user = authService.getUserDetailsByEmail(authentication.getName());
        service.deleteBooking(bookingId, user.id(), user.role());
    }
}
