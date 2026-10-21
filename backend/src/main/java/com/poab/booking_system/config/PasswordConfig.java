package com.poab.booking_system.config;

import com.poab.booking_system.user.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordHasher passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        return new PasswordHasher() {
            @Override
            public String encode(String password) {
                return bCryptPasswordEncoder.encode(password);
            }
        };
    }
}
