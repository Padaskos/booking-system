package io.github.padaskos.bookingsystem.booking;

public class UserNotAllowedException extends RuntimeException {
    public UserNotAllowedException() {
        super("User is not allowed to do this operation");
    }
}
