package com.poab.booking_system.config;

import com.poab.booking_system.user.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    PasswordHasher passwordHasher(org.springframework.security.crypto.password.PasswordEncoder encoder) {
        return encoder::encode;
    }
}
