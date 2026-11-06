package com.poab.booking_system.booking;

import com.poab.booking_system.auth.AuthService;
import com.poab.booking_system.auth.dto.UserDetailsResponse;
import com.poab.booking_system.booking.dto.AvailabilityResponse;
import com.poab.booking_system.booking.dto.BookingDetailsResponse;
import com.poab.booking_system.booking.dto.CreateBookingRequest;
import com.poab.booking_system.helpers.PageableValidator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public void createBooking(@Valid @RequestBody CreateBookingRequest request, Authentication authentication) {
        UserDetailsResponse user = authService.getUserDetailsByEmail(authentication.getName());
        service.createBooking(
                request.resourceId(),
                user.id(),
                request.startTime(),
                request.endTime(),
                request.bookingDescription()
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

    @GetMapping("/me")
    public Page<BookingDetailsResponse> getMyBookings(
            @RequestParam BookingPeriod period,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        PageableValidator.validateWithoutSorting(
                pageable,
                page,
                size,
                "Sorting is not supported for personal bookings. Ordering is determined by the selected period."
        );        UserDetailsResponse user = authService.getUserDetailsByEmail(authentication.getName());
        return service.getMyBookings(user.id(), period, pageable);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable UUID bookingId, Authentication authentication) {
        UserDetailsResponse user = authService.getUserDetailsByEmail(authentication.getName());
        service.deleteBooking(bookingId, user.id(), user.role());
    }
}
