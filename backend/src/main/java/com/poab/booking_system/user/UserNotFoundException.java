package com.poab.booking_system.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User with ID " + id + " was not found");
    }
}
