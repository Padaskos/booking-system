package io.github.padaskos.bookingsystem.user;

@FunctionalInterface
public interface PasswordHasher {
    String encode(String password);
}
