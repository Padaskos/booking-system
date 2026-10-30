package com.poab.booking_system.booking;

import java.util.UUID;

public class ResourceNotAvailableException extends RuntimeException {
    public ResourceNotAvailableException(UUID id) {
        super("Resource with id '" + id + "' is not active");
    }
}
