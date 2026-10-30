package com.poab.booking_system.user;

import com.poab.booking_system.auth.UnauthorizedCredentials;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordHasher passwordHasher;

    UserService (
        UserRepository repository,
        PasswordHasher passwordHasher
    ) {
        this.repository = repository;
        this.passwordHasher = passwordHasher;
    }

    public void createUser(String name, String email, String password) {
        if (repository.existsByEmailIgnoreCase(email))
            throw new EmailAlreadyExistsConflict(email);
        repository.save(new UserEntity(name, email, passwordHasher.encode(password)));
    }

    public UserEntity getUserByEmail(String email) {
        if (!repository.existsByEmailIgnoreCase(email))
            throw new UnauthorizedCredentials();
        return repository.getUserEntitiesByEmailIgnoreCase(email);
    }
}
