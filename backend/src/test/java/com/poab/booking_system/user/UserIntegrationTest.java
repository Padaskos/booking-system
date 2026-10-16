package com.poab.booking_system.user;

import com.poab.booking_system.user.dto.CreateUserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository repository;

    @Test
    void createUser_shouldReturn201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("Paris", "paris@poab.be", "123password123")
                        )))
                .andExpect(status().isCreated());
        assertThat(repository.existsByEmailIgnoreCase("paris@poab.be")).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidRegistrationRequests")
    void createUser_shouldReturn400_whenRequestIsInvalid(CreateUserRequest request) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidRegistrationRequests() {
        return Stream.of(
                Arguments.of(new CreateUserRequest(null, "paris@poab.be", "123password123")),
                Arguments.of(new CreateUserRequest("", "paris@poab.be", "123password123")),
                Arguments.of(new CreateUserRequest("a".repeat(101), "paris@poab.be", "123password123")),
                Arguments.of(new CreateUserRequest("Paris", null, "123password123")),
                Arguments.of(new CreateUserRequest("Paris", "", "123password123")),
                Arguments.of(new CreateUserRequest("Paris", "a".repeat(250) + "@a.be", "123password123")),
                Arguments.of(new CreateUserRequest("Paris", "parispoab.be", "123password123")),
                Arguments.of(new CreateUserRequest("Paris", "paris@poab.be", null)),
                Arguments.of(new CreateUserRequest("Paris", "paris@poab.be", "password")),
                Arguments.of(new CreateUserRequest("Paris", "paris@poab.be", "a".repeat(73)))
        );
    }

    @Test
    void createUser_shouldReturn409_whenEmailIsNotUnique() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("Paris", "paris@poab.be", "123password123")
                        )))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("Second", "paris@poab.be", "123password123")
                        )))
                .andExpect(status().isConflict());
    }
}
