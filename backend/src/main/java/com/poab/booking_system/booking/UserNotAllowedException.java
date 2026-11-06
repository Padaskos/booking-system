package com.poab.booking_system.booking;

public class UserNotAllowedException extends RuntimeException {
    public UserNotAllowedException() {
        super("User is not allowed to do this operation");
    }
}
