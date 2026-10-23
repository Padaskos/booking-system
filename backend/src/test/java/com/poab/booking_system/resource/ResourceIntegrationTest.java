
package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import com.poab.booking_system.resource.dto.ResourceResponse;
import com.poab.booking_system.resource.dto.UpdateStatusRequest;
import jakarta.transaction.Transactional;
import org.assertj.core.api.AssertionsForClassTypes;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class ResourceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17");
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.saveAll(List.of(
                new ResourceEntity("Laptop A", genericDescription, "Antwerpen", ResourceType.LAPTOP),
                new ResourceEntity("Laptop B", genericDescription, "Gent", ResourceType.LAPTOP),
                new ResourceEntity("Camera A", genericDescription, "Brussel", ResourceType.CAMERA)
        ));
    }

    private record PageResponse<T>(
            List<T> content,
            long totalElements,
            int totalPages
    ) {
    }

    private PageResponse<ResourceResponse> readResourcePage(MvcResult result) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                }
        );
    }

    private static final String genericName = "Name";
    private static final String genericDescription = "Description";
    private static final String genericLocation = "Location";
    private static final ResourceType genericResourceType = ResourceType.LAPTOP;

    private static ResourceEntity createResource() {
        return new ResourceEntity(genericName, genericDescription, genericLocation, genericResourceType);
    }

    private static ResourceEntity createResource(String name, ResourceType type) {
        return new ResourceEntity(name, genericDescription, genericLocation, type);
    }

    private static CreateResourceRequest createDto() {
        return new CreateResourceRequest(genericName, genericDescription, genericLocation, genericResourceType);
    }

    private static CreateResourceRequest createDto(String name, ResourceType type) {
        return new CreateResourceRequest(name, genericDescription, genericLocation, type);
    }

    private void assertResourceEntity(ResourceEntity entity) {
        assertThat(entity.getName()).isEqualTo(genericName);
        assertThat(entity.getDescription()).isEqualTo(genericDescription);
        assertThat(entity.getLocation()).isEqualTo(genericLocation);
        assertThat(entity.getType()).isEqualTo(genericResourceType);
    }

    @Test
    void createResource_shouldReturn201_whenRequestIsValid() throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto())))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidCreateResourceRequests")
    void createResource_shouldReturn400_whenRequestIsInvalid(CreateResourceRequest request) throws Exception {
        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidCreateResourceRequests() {
        return Stream.of(
                Arguments.of(createDto(null, genericResourceType)),
                Arguments.of(createDto("", genericResourceType)),
                Arguments.of(createDto(" ", genericResourceType)),
                Arguments.of(createDto("a".repeat(101), genericResourceType)),
                Arguments.of(createDto(genericName, null))
        );
    }

    @Test
    void getResources_shouldReturn200_withAllActiveResources() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(3);
    }

    @Test
    void getResources_shouldFilterByType() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("type", "LAPTOP"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content())
                .allMatch(resource -> resource.type() == ResourceType.LAPTOP);
    }

    @Test
    void getResources_shouldFilterByName_caseInsensitive() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("name", "laptop"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .containsExactlyInAnyOrder("Laptop A", "Laptop B");
    }

    @Test
    void getResources_shouldFilterByLocation() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("location", "gent"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content())
                .extracting(ResourceResponse::location)
                .containsOnly("Gent");
    }

    @Test
    void getResources_shouldFilterByNameAndType_combined() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("name", "Camera")
                        .param("type", "CAMERA"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .containsOnly("Camera A");
        assertThat(response.content())
                .extracting(ResourceResponse::type)
                .containsOnly(ResourceType.CAMERA);
    }

    @Test
    void getResources_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("name", "nonexistent"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(0);
    }

    @Test
    void getResources_shouldRespectPagination() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void getResources_shouldNotReturnInactiveResources() throws Exception {
        ResourceEntity inactive = createResource();
        inactive.setActive(false);
        repository.save(inactive);

        MvcResult result = mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .doesNotContain(genericName);
    }

    @Test
    void getResources_shouldSortByNameAscending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);


        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .containsExactly(
                        "Camera A",
                        "Laptop A",
                        "Laptop B"
                );
    }

    @Test
    void getResources_shouldSortByLocationDescending() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/resources")
                        .param("sort", "location,desc"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.content())
                .extracting(ResourceResponse::location)
                .containsExactly(
                        "Gent",
                        "Brussel",
                        "Antwerpen"
                );
    }

    @ParameterizedTest
    @MethodSource("invalidGetResourcesParams")
    void getResources_shouldReturn400_whenParamsAreInvalid(String paramName, String paramValue) throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param(paramName, paramValue))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidGetResourcesParams() {
        return Stream.of(
                Arguments.of("page", "-1"),
                Arguments.of("size", "101"),
                Arguments.of("size", "0"),
                Arguments.of("type", "INVALID_TYPE"),
                Arguments.of("sort", "description,asc"),
                Arguments.of("sort", "id,asc"),
                Arguments.of("sort", "isActive,asc")
        );
    }

    private void assertResourceEntity(ResourceEntity entity, DetailedResourceResponse request) {
        assertThat(entity.getName()).isEqualTo(request.name());
        assertThat(entity.getDescription()).isEqualTo(request.description());
        assertThat(entity.getLocation()).isEqualTo(request.location());
        assertThat(entity.getType()).isEqualTo(request.type());
        assertThat(entity.getId()).isEqualTo(request.id());
        AssertionsForClassTypes.assertThat(request.isActive()).isTrue();
        AssertionsForClassTypes.assertThat(entity.getCreationTimestamp()).isEqualTo(request.creationTimestamp());
        assertThat(repository.findById(entity.getId()).orElseThrow().getUpdatedTimestamp())
                .isAfterOrEqualTo(request.updatedTimestamp());
    }

    @Test
    void getResources_shouldReturn400_whenSizeExceedsLimit()
            throws Exception {

        mockMvc.perform(get("/api/resources")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResources_shouldReturn400_whenTypeIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/resources")
                        .param("type", "INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResources_shouldReturn400_whenSortFieldIsInvalid()
            throws Exception {

        mockMvc.perform(get("/api/resources")
                        .param("sort", "description,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResourceById_shouldReturnResource_whenResourceExists() throws Exception {
        ResourceEntity saved = repository.saveAndFlush(
                createResource("Projector", ResourceType.CAMERA)
        );

        MvcResult result = mockMvc.perform(
                        get("/api/resources/{id}", saved.getId())
                )
                .andExpect(status().isOk())
                .andReturn();

        DetailedResourceResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                DetailedResourceResponse.class);

        assertResourceEntity(saved, response);
    }

    @Test
    void getResourceById_shouldReturn404_whenResourceDoesNotExist() throws Exception {

        UUID unknownId = UUID.randomUUID();

        mockMvc.perform(get("/api/resources/{id}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getResourceById_shouldReturn400_whenIdIsMalformed() throws Exception {

        mockMvc.perform(get("/api/resources/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResourceById_shouldReturnInactiveResource() throws Exception {

        ResourceEntity inactive = createResource();

        inactive.setActive(false);
        ResourceEntity saved = repository.saveAndFlush(inactive);

        MvcResult result = mockMvc.perform(
                        get("/api/resources/{id}", saved.getId())
                )
                .andExpect(status().isOk())
                .andReturn();

        DetailedResourceResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                DetailedResourceResponse.class);

        assertThat(response.id()).isEqualTo(saved.getId());
        assertFalse(response.isActive());
    }

    private ResourceEntity saveResource(boolean active) {
        ResourceEntity entity = new ResourceEntity("name", "description", "location", ResourceType.CAMERA);
        entity.setActive(active);
        return repository.saveAndFlush(entity);
    }

    private void patchStatus(UUID id, boolean status) throws Exception {
        mockMvc.perform(patch("/api/resources/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(status))))
                .andExpect(status().isOk());
    }

    @Test
    void updateResourceStatusById_StatusToActive() throws Exception {
        ResourceEntity saved = saveResource(false);
        patchStatus(saved.getId(), true);
        assertTrue(repository.findById(saved.getId()).orElseThrow().isActive());
    }

    @Test
    void updateResourceStatusById_StatusToInactive() throws Exception {
        ResourceEntity saved = saveResource(true);
        patchStatus(saved.getId(), false);
        assertFalse(repository.findById(saved.getId()).orElseThrow().isActive());
    }

    @Test
    void updateResourceStatusById_idNotFound() throws Exception {
        mockMvc.perform(patch("/api/resources/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(false))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateResourceStatusById_invalidRequest() throws Exception {
        mockMvc.perform(patch("/api/resources/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateStatusRequest(null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateResource_shouldReturnUpdatedResource_whenRequestIsValid() throws Exception {

        ResourceEntity saved = repository.saveAndFlush(createResource());
        CreateResourceRequest request = new CreateResourceRequest("New name", "New description", "Gent", ResourceType.CAMERA);

        MvcResult result = mockMvc.perform(
                        put("/api/resources/{id}", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn();

        DetailedResourceResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                DetailedResourceResponse.class
        );

        assertResourceEntity(saved, response);
    }

    @Test
    void updateResource_shouldReturn404_whenResourceDoesNotExist() throws Exception {
        mockMvc.perform(
                        put("/api/resources/{id}", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateResource_shouldReturn400_whenIdIsMalformed() throws Exception {
        mockMvc.perform(
                        put("/api/resources/not-a-uuid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createDto())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateResource_shouldReturn400_andNotUpdate_whenRequestIsInvalid()
            throws Exception {

        ResourceEntity saved = repository.saveAndFlush(
                createResource());

        CreateResourceRequest invalidRequest = createDto(" ", genericResourceType);

        mockMvc.perform(
                        put("/api/resources/{id}", saved.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                )
                .andExpect(status().isBadRequest());

        ResourceEntity unchanged = repository.findById(saved.getId()).orElseThrow();

        assertResourceEntity(unchanged);
    }
}