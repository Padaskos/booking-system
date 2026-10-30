package com.poab.booking_system.user;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public DatabaseUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity entity = repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return User.withUsername(entity.getEmail())
                .password(entity.getPasswordHash())
                .roles(entity.getRole().name())
                .build();
    }
}