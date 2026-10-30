package com.poab.booking_system.user;

@FunctionalInterface
public interface PasswordHasher {
    String encode(String password);
}
