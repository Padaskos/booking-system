package io.github.padaskos.bookingsystem.booking;

import io.github.padaskos.bookingsystem.booking.dto.AvailabilityResponse;
import io.github.padaskos.bookingsystem.booking.dto.CreateBookingRequest;
import io.github.padaskos.bookingsystem.resource.ResourceEntity;
import io.github.padaskos.bookingsystem.resource.ResourceRepository;
import io.github.padaskos.bookingsystem.resource.ResourceType;
import io.github.padaskos.bookingsystem.user.UserEntity;
import io.github.padaskos.bookingsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class BookingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ResourceEntity resource;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        resource = resourceRepository.saveAndFlush(createResource());
        user = userRepository.saveAndFlush(createUser());
    }

    @Test
    void createBooking_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateBookingRequest request = createDto(resource.getId(), genericStartTime, genericEndTime);

        BookingEntity saved = postBooking(request);

        assertThat(bookingRepository.findAll()).hasSize(1);

        assertThat(saved.getResourceId()).isEqualTo(resource.getId());
        assertThat(saved.getUserId()).isEqualTo(user.getId());
        assertThat(saved.getStartTime()).isEqualTo(genericStartTime);
        assertThat(saved.getEndTime()).isEqualTo(genericEndTime);
        assertThat(saved.getBookingDescription()).isEqualTo(genericBookingDescription);

    }

    @ParameterizedTest
    @MethodSource("invalidCreateBookingRequests")
    void createBooking_shouldReturn400_whenRequestIsInvalid(CreateBookingRequest request) throws Exception {
        postBookingRequest(request).andExpect(status().isBadRequest());

        assertThat(bookingRepository.findAll()).isEmpty();
    }

    private static Stream<Arguments> invalidCreateBookingRequests() {
        return Stream.of(
                Arguments.of(createDto(null, genericStartTime, genericEndTime)),
                Arguments.of(createDto(UUID.randomUUID(), null, genericEndTime)),
                Arguments.of(createDto(UUID.randomUUID(), genericStartTime, null)),
                Arguments.of(new CreateBookingRequest(
                        UUID.randomUUID(),
                        genericStartTime,
                        genericEndTime,
                        null
                )),
                Arguments.of(new CreateBookingRequest(
                        UUID.randomUUID(),
                        genericStartTime,
                        genericEndTime,
                        ""
                )),
                Arguments.of(new CreateBookingRequest(
                        UUID.randomUUID(),
                        genericStartTime,
                        genericEndTime,
                        " "
                )),
                Arguments.of(new CreateBookingRequest(
                        UUID.randomUUID(),
                        genericStartTime,
                        genericEndTime,
                        "a".repeat(501)
                ))
        );
    }

    @Test
    void createBooking_shouldReturn400_whenStartIsNotBeforeEnd() throws Exception {
        postBookingRequest(createDto(resource.getId(), genericEndTime, genericStartTime))
                .andExpect(status().isBadRequest());

        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    void createBooking_shouldReturn400_whenStartIsInThePast() throws Exception {
        postBookingRequest(createDto(resource.getId(), Instant.now().minusSeconds(300), genericStartTime))
                .andExpect(status().isBadRequest());

        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    void createBooking_shouldReturn400_whenRequestedPeriodOverlapsExistingBooking() throws Exception {
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                genericStartTime,
                genericEndTime,
                genericBookingDescription
        ));

        postBookingRequest(createDto(
                resource.getId(),
                genericStartTime.plusSeconds(100),
                genericEndTime.plusSeconds(100)
        )).andExpect(status().isBadRequest());

        assertThat(bookingRepository.findAll()).hasSize(1);
    }

    @Test
    void createBooking_shouldReturn404_whenResourceDoesNotExist() throws Exception {
        postBookingRequest(createDto(UUID.randomUUID(), genericStartTime, genericEndTime))
                .andExpect(status().isNotFound());

        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    void createBooking_shouldReturn403_whenUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/booking")
                        .with(user("unknown@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto(
                                resource.getId(),
                                genericStartTime,
                                genericEndTime
                        ))))
                .andExpect(status().isUnauthorized());

        assertThat(bookingRepository.findAll()).isEmpty();
    }

    @Test
    void isTimeSlotAvailable_shouldReturnTrue_whenRequestIsValidAndSlotIsFree() throws Exception {
        AvailabilityResponse response = getAvailability(
                resource.getId(),
                genericStartTime,
                genericEndTime
        );

        assertThat(response.available()).isTrue();
    }

    @Test
    void isTimeSlotAvailable_shouldReturnFalse_whenBookingOverlaps() throws Exception {
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                genericStartTime,
                genericEndTime,
                genericBookingDescription
        ));

        AvailabilityResponse response = getAvailability(
                resource.getId(),
                Instant.now().plusSeconds(400),
                Instant.now().plusSeconds(700)
        );

        assertThat(response.available()).isFalse();
    }

    @Test
    void isTimeSlotAvailable_shouldReturnFalse_whenResourceIsInactive() throws Exception {
        resource.setActive(false);
        resourceRepository.saveAndFlush(resource);

        AvailabilityResponse response = getAvailability(
                resource.getId(),
                genericStartTime,
                genericEndTime
        );

        assertThat(response.available()).isFalse();
    }

    @Test
    void isTimeSlotAvailable_shouldReturnFalse_whenTimeInputIsInvalid() throws Exception {
        AvailabilityResponse response = getAvailability(
                resource.getId(),
                genericEndTime,
                genericStartTime
        );

        assertThat(response.available()).isFalse();
    }

    @Test
    void isTimeSlotAvailable_shouldReturn404_whenResourceDoesNotExist() throws Exception {
        getAvailabilityRequest(UUID.randomUUID(),genericStartTime,genericEndTime)
                .andExpect(status().isNotFound());
    }

    @Test
    void isTimeSlotAvailable_shouldReturn400_whenResourceIdIsMalformed() throws Exception {
        mockMvc.perform(get("/booking/availability/{resourceId}", "not-a-uuid")
                        .param("startTime",genericStartTime.toString())
                        .param("endTime",genericEndTime.toString())
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBooking_shouldReturn204_whenUserOwnsBooking() throws Exception {
        BookingEntity booking = bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                genericStartTime,
                genericEndTime,
                genericBookingDescription
        ));

        deleteBooking(booking.id).andExpect(status().isNoContent());

        assertThat(bookingRepository.findById(booking.id)).isEmpty();
    }

    @Test
    void deleteBooking_shouldReturn404_whenBookingDoesNotExist() throws Exception {
        deleteBooking(UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void deleteBooking_shouldReturn400_whenBookingIdIsMalformed() throws Exception {
        mockMvc.perform(delete("/booking/{bookingId}", "not-a-uuid")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBooking_shouldReturn403_whenUserDoesNotOwnBooking() throws Exception {
        UserEntity otherUser = userRepository.saveAndFlush(new UserEntity(
                "Other User",
                "other@example.com",
                "password"
        ));
        BookingEntity booking = bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                otherUser.getId(),
                genericStartTime,
                genericEndTime,
                genericBookingDescription
        ));

        deleteBooking(booking.id).andExpect(status().isForbidden());

        assertThat(bookingRepository.findById(booking.id)).isPresent();
    }

    @Test
    void getMyBookings_shouldReturnUpcomingBookings_whenPeriodIsUpcoming() throws Exception {
        BookingEntity booking = bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().plusSeconds(300),
                Instant.now().plusSeconds(600),
                genericBookingDescription
        ));

        ObjectNode response = getMyBookings("UPCOMING", 0, 20);

        assertThat(response.get("content")).hasSize(1);
        assertThat(response.get("content").get(0).get("resourceId").asString()).isEqualTo(resource.getId().toString());
        assertThat(response.get("content").get(0).get("resourceName").asString()).isEqualTo(resource.getName());
        assertThat(response.get("content").get(0).get("bookingDescription").asString())
                .isEqualTo(genericBookingDescription);
        assertThat(response.get("content").get(0).get("startTime").asString()).isEqualTo(booking.getStartTime().toString());
        assertThat(response.get("content").get(0).get("endTime").asString()).isEqualTo(booking.getEndTime().toString());
        assertThat(response.get("totalElements").asInt()).isEqualTo(1);
    }

    @Test
    void getMyBookings_shouldReturnPreviousBookings_whenPeriodIsPrevious() throws Exception {
        BookingEntity booking = bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(300),
                genericBookingDescription
        ));

        ObjectNode response = getMyBookings("PREVIOUS", 0, 20);

        assertThat(response.get("content")).hasSize(1);
        assertThat(response.get("content").get(0).get("resourceId").asString()).isEqualTo(resource.getId().toString());
        assertThat(response.get("content").get(0).get("resourceName").asString()).isEqualTo(resource.getName());
        assertThat(response.get("content").get(0).get("bookingDescription").asString())
                .isEqualTo(genericBookingDescription);
        assertThat(response.get("content").get(0).get("startTime").asString()).isEqualTo(booking.getStartTime().toString());
        assertThat(response.get("content").get(0).get("endTime").asString()).isEqualTo(booking.getEndTime().toString());
        assertThat(response.get("totalElements").asInt()).isEqualTo(1);
    }

    @Test
    void getMyBookings_shouldOnlyReturnBookingsForAuthenticatedUser() throws Exception {
        UserEntity otherUser = userRepository.saveAndFlush(new UserEntity(
                "Other User",
                "other@example.com",
                "password"
        ));
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().plusSeconds(300),
                Instant.now().plusSeconds(600),
                genericBookingDescription
        ));
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                otherUser.getId(),
                Instant.now().plusSeconds(900),
                Instant.now().plusSeconds(1200),
                "Other user booking"
        ));

        ObjectNode response = getMyBookings("UPCOMING", 0, 20);

        assertThat(response.get("content")).hasSize(1);
        assertThat(response.get("content").get(0).get("bookingDescription").asString())
                .isEqualTo(genericBookingDescription);
        assertThat(response.get("totalElements").asInt()).isEqualTo(1);
    }

    @Test
    void getMyBookings_shouldKeepPageMetadata() throws Exception {
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().plusSeconds(300),
                Instant.now().plusSeconds(600),
                "First booking"
        ));
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().plusSeconds(900),
                Instant.now().plusSeconds(1200),
                "Second booking"
        ));
        bookingRepository.saveAndFlush(new BookingEntity(
                resource.getId(),
                user.getId(),
                Instant.now().plusSeconds(1500),
                Instant.now().plusSeconds(1800),
                "Third booking"
        ));

        ObjectNode response = getMyBookings("UPCOMING", 1, 2);

        assertThat(response.get("content")).hasSize(1);
        assertThat(response.get("totalElements").asInt()).isEqualTo(3);
        assertThat(response.get("totalPages").asInt()).isEqualTo(2);
        assertThat(response.get("number").asInt()).isEqualTo(1);
        assertThat(response.get("size").asInt()).isEqualTo(2);
    }

    @Test
    void getMyBookings_shouldReturn400_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .param("period", "UPCOMING")
                        .param("page", "-1")
                        .param("size", "20")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBookings_shouldReturn400_whenSizeIsLessThanOne() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .param("period", "UPCOMING")
                        .param("page", "0")
                        .param("size", "0")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBookings_shouldReturn400_whenSizeIsGreaterThanOneHundred() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .param("period", "UPCOMING")
                        .param("page", "0")
                        .param("size", "101")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBookings_shouldReturn400_whenSortingIsRequested() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .param("period", "UPCOMING")
                        .param("sort", "startTime,asc")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBookings_shouldReturn400_whenPeriodIsMissing() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBookings_shouldReturn400_whenPeriodIsInvalid() throws Exception {
        mockMvc.perform(get("/booking/me")
                        .param("period", "INVALID")
                        .with(asUser()))
                .andExpect(status().isBadRequest());
    }

    private static final String genericName = "Name";
    private static final String genericDescription = "Description";
    private static final String genericLocation = "Location";
    private static final ResourceType genericResourceType = ResourceType.LAPTOP;
    private static final String genericUserName = "Test User";
    private static final String genericUserEmail = "test-user@example.com";
    private static final String genericPasswordHash = "password";
    private static final String genericBookingDescription = "Booking";
    private static final Instant genericStartTime = Instant.now().plusSeconds(300);
    private static final Instant genericEndTime = Instant.now().plusSeconds(600);

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asUser() {
        return user(genericUserEmail).roles("USER");
    }

    private static ResourceEntity createResource() {
        return new ResourceEntity(genericName, genericDescription, genericLocation, genericResourceType);
    }

    private static UserEntity createUser() {
        return new UserEntity(genericUserName, genericUserEmail, genericPasswordHash);
    }

    private static CreateBookingRequest createDto(UUID resourceId, Instant start, Instant end) {
        return new CreateBookingRequest(resourceId, start, end, genericBookingDescription);
    }

    private BookingEntity postBooking(CreateBookingRequest request) throws Exception {
        postBookingRequest(request).andExpect(status().isCreated());

        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getResourceId().equals(request.resourceId()))
                .filter(booking -> booking.getStartTime().equals(request.startTime()))
                .filter(booking -> booking.getEndTime().equals(request.endTime()))
                .filter(booking -> booking.getBookingDescription().equals(request.bookingDescription()))
                .findFirst()
                .orElseThrow();
    }

    private ResultActions postBookingRequest(CreateBookingRequest request) throws Exception {
        return mockMvc.perform(post("/booking")
                .with(asUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getAvailabilityRequest(UUID resourceId, Instant start, Instant end) throws Exception {
        return mockMvc.perform(get("/booking/availability/{resourceId}", resourceId)
                .param("startTime", start.toString())
                .param("endTime", end.toString())
                .with(asUser()));
    }

    private AvailabilityResponse getAvailability(UUID resourceId, Instant start, Instant end) throws Exception {
        MvcResult result = getAvailabilityRequest(resourceId, start, end)
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), AvailabilityResponse.class);
    }

    private ObjectNode getMyBookings(String period, int page, int size) throws Exception {
        MvcResult result = mockMvc.perform(get("/booking/me")
                        .param("period", period)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .with(asUser()))
                .andExpect(status().isOk())
                .andReturn();

        return (ObjectNode) objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private ResultActions deleteBooking(UUID bookingId) throws Exception {
        return mockMvc.perform(delete("/booking/{bookingId}", bookingId)
                .with(asUser()));
    }
}