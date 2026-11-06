package io.github.padaskos.bookingsystem.resource;

import java.util.UUID;

public class ResourceUnavailableException extends RuntimeException {
    public ResourceUnavailableException(UUID id) {
        super("Resource with ID " + id + " is not available");
    }
}
