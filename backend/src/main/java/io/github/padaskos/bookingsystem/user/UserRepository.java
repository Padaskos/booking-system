package io.github.padaskos.bookingsystem.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    List<UserRepository.UserEmailView> findAllByIdIn(Collection<UUID> ids);

    interface UserEmailView {
        UUID getId();
        String getEmail();
    }
}
