package com.poab.booking_system.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldSaveUser_whenEmailIsUnique() {
        when(repository.existsByEmailIgnoreCase("paris@poab.be")).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");

        userService.createUser("Paris", "paris@poab.be", "123password123");

        verify(repository).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        when(repository.existsByEmailIgnoreCase("paris@poab.be")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("Paris", "paris@poab.be", "123password123"))
                .isInstanceOf(EmailAlreadyExistsConflict.class);
    }

    @Test
    void createUser_shouldNotSave_whenEmailAlreadyExists() {
        when(repository.existsByEmailIgnoreCase("paris@poab.be")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("Paris", "paris@poab.be", "123password123"))
                .isInstanceOf(EmailAlreadyExistsConflict.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createUser_shouldHashPassword_beforeSaving() {
        when(repository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode("123password123")).thenReturn("hashed");

        userService.createUser("Paris", "paris@poab.be", "123password123");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void createUser_shouldAssignUserRole() {
        when(repository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(passwordHasher.encode(any())).thenReturn("hashed");

        userService.createUser("Paris", "paris@poab.be", "123password123");

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
    }
}
