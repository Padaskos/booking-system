package com.poab.booking_system.auth;

import com.poab.booking_system.auth.dto.LoginRequest;
import com.poab.booking_system.auth.dto.UserDetailsResponse;
import com.poab.booking_system.user.UserRepository;
import com.poab.booking_system.auth.dto.CreateUserRequest;
import com.poab.booking_system.user.UserRole;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
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
public class AuthIntegrationTest {

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

    @Autowired
    private AuthService authService;

    @Test
    void registerUser_shouldReturn201_whenRequestIsValid() throws Exception {
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
    void registerUser_shouldReturn400_whenRequestIsInvalid(CreateUserRequest request) throws Exception {
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
    void registerUser_shouldReturn409_whenEmailIsNotUnique() throws Exception {
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
    @Test
    void login_shouldReturnUserDetails_whenCredentialsAreValid() throws Exception {
        registerTestUser();

        MvcResult result = performLogin(
                new LoginRequest("paris@poab.be", "123password123")
        )
                .andExpect(status().isOk())
                .andReturn();

        UserDetailsResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserDetailsResponse.class
        );

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Paris");
        assertThat(response.email()).isEqualTo("paris@poab.be");
        assertThat(response.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void login_shouldReturn401_whenPasswordIsWrong() throws Exception {
        registerTestUser();

        performLogin(new LoginRequest(
                "paris@poab.be",
                "wrongpassword1"
        )).andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401_whenUserDoesNotExist() throws Exception {
        performLogin(new LoginRequest(
                "ghost@poab.be",
                "123password123"
        )).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("invalidLoginRequests")
    void login_shouldReturn400_whenRequestIsInvalid(LoginRequest request)
            throws Exception {

        performLogin(request)
                .andExpect(status().isBadRequest());
    }

    private ResultActions performLogin(LoginRequest request) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private void registerTestUser() {
        authService.registerUser(
                "Paris",
                "paris@poab.be",
                "123password123"
        );
    }

    private static Stream<Arguments> invalidLoginRequests() {
        return Stream.of(
                Arguments.of(new LoginRequest(null, "123password123")),
                Arguments.of(new LoginRequest("", "123password123")),
                Arguments.of(new LoginRequest("parispoab.be", "123password123")),
                Arguments.of(new LoginRequest("paris@poab.be", null)),
                Arguments.of(new LoginRequest("paris@poab.be", "short")),
                Arguments.of(new LoginRequest("paris@poab.be", "a".repeat(73)))
        );
    }
}

