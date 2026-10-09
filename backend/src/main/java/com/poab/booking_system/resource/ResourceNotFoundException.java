package com.poab.booking_system.resource;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(UUID id) {
        super("Resource with ID " + id + " was not found");
    }
}
