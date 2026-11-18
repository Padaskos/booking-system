package io.github.padaskos.bookingsystem.user;

import io.github.padaskos.bookingsystem.auth.UnauthorizedCredentials;
import io.github.padaskos.bookingsystem.user.dto.UserEmailResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

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

    @Transactional
    public void createUser(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsConflict(normalizedEmail);
        }

        repository.saveAndFlush(new UserEntity(
                name,
                normalizedEmail,
                passwordHasher.encode(password)
        ));
    }

    public UserEntity getUserByEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(UnauthorizedCredentials::new);
    }

    public List<UserEmailResponse> getUserEmailById(List<UUID> userIds) {
        return repository.findAllByIdIn(userIds)
                .stream().map(user -> new UserEmailResponse(
                        user.getId(),
                        user.getEmail()
                ))
                .toList();
    }
}
