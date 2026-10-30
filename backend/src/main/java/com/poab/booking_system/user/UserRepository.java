package com.poab.booking_system.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmailIgnoreCase(String email);
    UserEntity getUserEntitiesByEmailIgnoreCase(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
