package com.poab.booking_system.auth;

import com.poab.booking_system.user.UserEntity;
import com.poab.booking_system.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;

    AuthService(
            UserService userService
    ) {
        this.userService = userService;
    }

    public UserEntity login(String email, String password) {
        return userService.getUserByEmail(email);
    }
}
