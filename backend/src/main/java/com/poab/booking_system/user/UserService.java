package com.poab.booking_system.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    UserService (
        UserRepository repository,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(String name, String email, String password) {
        repository.save(new UserEntity(
                        name,
                        email,
                        passwordEncoder.encode(password)
                ));
    }
}
