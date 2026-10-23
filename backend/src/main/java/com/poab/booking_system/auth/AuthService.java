package com.poab.booking_system.auth;

import com.poab.booking_system.auth.dto.UserDetailsResponse;
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

    public void registerUser(String name, String email, String password) {
        userService.createUser(name, email, password);
    }

    public UserDetailsResponse getUserDetailsByEmail(String email) {
        UserEntity entity = userService.getUserByEmail(email);

        return new UserDetailsResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole()
        );
    }
}
