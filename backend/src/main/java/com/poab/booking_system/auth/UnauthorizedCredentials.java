package com.poab.booking_system.auth;

public class UnauthorizedCredentials extends RuntimeException {
    public UnauthorizedCredentials() {
        super("Invalid Email or Password");
    }
}
