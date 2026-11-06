package io.github.padaskos.bookingsystem.auth;

public class UnauthorizedCredentials extends RuntimeException {
    public UnauthorizedCredentials() {
        super("Invalid Email or Password");
    }
}
