package io.github.padaskos.bookingsystem.booking;

import java.util.UUID;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(UUID id) {
        super("Booking with id '" + id + "' was not found");
    }
}
