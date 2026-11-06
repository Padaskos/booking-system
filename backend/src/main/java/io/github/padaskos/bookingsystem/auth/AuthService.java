package io.github.padaskos.bookingsystem.auth;

import io.github.padaskos.bookingsystem.auth.dto.UserDetailsResponse;
import io.github.padaskos.bookingsystem.user.UserEntity;
import io.github.padaskos.bookingsystem.user.UserService;
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
