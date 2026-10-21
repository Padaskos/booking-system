package com.poab.booking_system.auth.dto;

import com.poab.booking_system.user.UserRole;

import java.util.UUID;

public record UserDetailsResponse(
        UUID id,
        String name,
        String email,
        UserRole role
) {
}
