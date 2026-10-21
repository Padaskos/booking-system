package com.poab.booking_system.auth;

import com.poab.booking_system.auth.dto.CreateUserRequest;
import com.poab.booking_system.auth.dto.LoginRequest;
import com.poab.booking_system.auth.dto.UserDetailsResponse;
import com.poab.booking_system.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request.name(), request.email(), request.password());
    }

//    @PostMapping("/login")
//    public UserDetailsResponse login(@Valid @RequestBody LoginRequest request) {
//        return authService.login(request.email(), request.password())
//                .(entity -> new UserDetailsResponse(
//                        entity.getId(),
//                        entity.getName(),
//                        entity.getEmail(),
//                        entity.role()
//                ));
//    }
}
