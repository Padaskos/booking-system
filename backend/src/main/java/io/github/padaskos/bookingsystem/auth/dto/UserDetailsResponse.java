package io.github.padaskos.bookingsystem.auth.dto;

import io.github.padaskos.bookingsystem.user.UserRole;

import java.util.UUID;

public record UserDetailsResponse(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}
