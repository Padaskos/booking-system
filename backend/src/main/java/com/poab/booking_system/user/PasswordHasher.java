package com.poab.booking_system.user;

public interface PasswordHasher {
    String encode(String password);
}
