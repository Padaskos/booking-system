package io.github.padaskos.bookingsystem.resource;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(UUID id) {
        super("Resource with ID " + id + " was not found");
    }
}
