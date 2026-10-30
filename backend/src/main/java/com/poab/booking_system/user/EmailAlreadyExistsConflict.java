package com.poab.booking_system.user;

public class EmailAlreadyExistsConflict extends RuntimeException {
    public EmailAlreadyExistsConflict(String email) {
        super("The email '" + email + "' is already linked with another user");
    }
}
