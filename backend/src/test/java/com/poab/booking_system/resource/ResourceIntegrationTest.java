package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import com.poab.booking_system.resource.dto.ResourceResponse;
import com.poab.booking_system.resource.dto.UpdateStatusRequest;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class ResourceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

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

    @Test
    void createResource_shouldReturn201_whenRequestIsValid() throws Exception {
        postResource(createDto()).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidCreateResourceRequests")
    void createResource_shouldReturn400_whenRequestIsInvalid(CreateResourceRequest request) throws Exception {
        postResource(request).andExpect(status().isBadRequest());
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
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams());

        assertThat(response.content()).hasSize(3);
        assertThat(response.totalElements()).isEqualTo(3);
    }

    @Test
    void getResources_shouldFilterByType() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("type", "LAPTOP"));

        assertThat(response.content()).hasSize(2);
        assertThat(response.content()).allMatch(resource -> resource.type() == ResourceType.LAPTOP);
    }

    @Test
    void getResources_shouldFilterByName_caseInsensitive() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("name", "laptop"));

        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .containsExactlyInAnyOrder("Laptop A", "Laptop B");
    }

    @Test
    void getResources_shouldFilterByLocation() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("location", "gent"));

        assertThat(response.content())
                .extracting(ResourceResponse::location)
                .containsOnly("Gent");
    }

    @Test
    void getResources_shouldFilterByNameAndType_combined() throws Exception {
        PageResponse<ResourceResponse> response =
                getResourcesPage(queryParams("name", "Camera", "type", "CAMERA"));

        assertThat(response.content()).extracting(ResourceResponse::name).containsOnly("Camera A");
        assertThat(response.content()).extracting(ResourceResponse::type).containsOnly(ResourceType.CAMERA);
    }

    @Test
    void getResources_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("name", "nonexistent"));

        assertThat(response.content()).isEmpty();
    }

    @Test
    void getResources_shouldRespectPagination() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("page", "0", "size", "2"));

        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(2);
    }

    @Test
    void getResources_shouldNotReturnInactiveResources() throws Exception {
        repository.save(inactiveResource(createResource()));

        PageResponse<ResourceResponse> response = getResourcesPage(queryParams());

        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.content()).extracting(ResourceResponse::name).doesNotContain(genericName);
    }

    @Test
    void getResources_shouldSortByNameAscending() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("sort", "name,asc"));

        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .containsExactly("Camera A", "Laptop A", "Laptop B");
    }

    @Test
    void getResources_shouldSortByLocationDescending() throws Exception {
        PageResponse<ResourceResponse> response = getResourcesPage(queryParams("sort", "location,desc"));

        assertThat(response.content())
                .extracting(ResourceResponse::location)
                .containsExactly("Gent", "Brussel", "Antwerpen");
    }

    @ParameterizedTest
    @MethodSource("invalidGetResourcesParams")
    void getResources_shouldReturn400_whenParamsAreInvalid(String paramName, String paramValue) throws Exception {
        getResourcesRequest(queryParams(paramName, paramValue)).andExpect(status().isBadRequest());
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

    @Test
    void getResourceById_shouldReturnResource_whenResourceExists() throws Exception {
        ResourceEntity saved = repository.saveAndFlush(createResource());

        DetailedResourceResponse response = getResourceById(saved.getId());

        assertResourceEntity(saved, response);
    }

    @Test
    void getResourceById_shouldReturn404_whenResourceDoesNotExist() throws Exception {
        getResourceByIdRequest(UUID.randomUUID()).andExpect(status().isNotFound());
    }

    @Test
    void getResourceById_shouldReturn400_whenIdIsMalformed() throws Exception {
        getResourceByIdRequest("not-a-uuid").andExpect(status().isBadRequest());
    }

    @Test
    void getResourceById_shouldReturnInactiveResource() throws Exception {
        ResourceEntity saved = repository.saveAndFlush(inactiveResource(createResource()));

        DetailedResourceResponse response = getResourceById(saved.getId());

        assertThat(response.id()).isEqualTo(saved.getId());
        assertFalse(response.isActive());
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
        patchStatusRequest(UUID.randomUUID(), false).andExpect(status().isNotFound());
    }

    @Test
    void updateResourceStatusById_invalidRequest() throws Exception {
        patchStatusRequest(UUID.randomUUID(), null).andExpect(status().isBadRequest());
    }

    @Test
    void updateResource_shouldReturnUpdatedResource_whenRequestIsValid() throws Exception {
        ResourceEntity saved = repository.saveAndFlush(createResource());
        CreateResourceRequest request =
                new CreateResourceRequest("New name", "New description", "Gent", ResourceType.CAMERA);

        DetailedResourceResponse response = putResource(saved.getId(), request);

        assertResourceEntity(saved, response);
    }

    @Test
    void updateResource_shouldReturn404_whenResourceDoesNotExist() throws Exception {
        putResourceRequest(UUID.randomUUID(), createDto()).andExpect(status().isNotFound());
    }

    @Test
    void updateResource_shouldReturn400_whenIdIsMalformed() throws Exception {
        putResourceRequest("not-a-uuid", createDto()).andExpect(status().isBadRequest());
    }

    @Test
    void updateResource_shouldReturn400_andNotUpdate_whenRequestIsInvalid() throws Exception {
        ResourceEntity saved = repository.saveAndFlush(createResource());
        CreateResourceRequest invalidRequest = createDto(" ", genericResourceType);

        putResourceRequest(saved.getId(), invalidRequest).andExpect(status().isBadRequest());

        assertResourceEntity(repository.findById(saved.getId()).orElseThrow());
    }

    private static final String genericName = "Name";
    private static final String genericDescription = "Description";
    private static final String genericLocation = "Location";
    private static final ResourceType genericResourceType = ResourceType.LAPTOP;

    private record PageResponse<T>(List<T> content, long totalElements, int totalPages) {}

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asUser() {
        return user("test-user").roles("USER");
    }

    private static SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor asAdmin() {
        return user("admin").roles("ADMIN");
    }

    private static MultiValueMap<String, String> queryParams(String... keyValuePairs) {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            params.add(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return params;
    }

    private static ResourceEntity createResource() {
        return new ResourceEntity(genericName, genericDescription, genericLocation, genericResourceType);
    }

    private static ResourceEntity inactiveResource(ResourceEntity entity) {
        entity.setActive(false);
        return entity;
    }

    private static CreateResourceRequest createDto() {
        return new CreateResourceRequest(genericName, genericDescription, genericLocation, genericResourceType);
    }

    private static CreateResourceRequest createDto(String name, ResourceType type) {
        return new CreateResourceRequest(name, genericDescription, genericLocation, type);
    }

    private ResourceEntity saveResource(boolean active) {
        ResourceEntity entity = createResource();
        entity.setActive(active);
        return repository.saveAndFlush(entity);
    }

    private void assertResourceEntity(ResourceEntity entity) {
        assertThat(entity.getName()).isEqualTo(genericName);
        assertThat(entity.getDescription()).isEqualTo(genericDescription);
        assertThat(entity.getLocation()).isEqualTo(genericLocation);
        assertThat(entity.getType()).isEqualTo(genericResourceType);
    }

    private void assertResourceEntity(ResourceEntity entity, DetailedResourceResponse response) {
        assertThat(entity.getName()).isEqualTo(response.name());
        assertThat(entity.getDescription()).isEqualTo(response.description());
        assertThat(entity.getLocation()).isEqualTo(response.location());
        assertThat(entity.getType()).isEqualTo(response.type());
        assertThat(entity.getId()).isEqualTo(response.id());
        assertThat(response.isActive()).isTrue();
        assertThat(entity.getCreationTimestamp()).isEqualTo(response.creationTimestamp());
        assertThat(repository.findById(entity.getId()).orElseThrow().getUpdatedTimestamp())
                .isAfterOrEqualTo(response.updatedTimestamp());
    }

    private ResultActions postResource(CreateResourceRequest request) throws Exception {
        return mockMvc.perform(post("/api/resources")
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getResourcesRequest(MultiValueMap<String, String> params) throws Exception {
        return mockMvc.perform(get("/api/resources").with(asUser()).params(params));
    }

    private PageResponse<ResourceResponse> getResourcesPage(MultiValueMap<String, String> params) throws Exception {
        MvcResult result = getResourcesRequest(params).andExpect(status().isOk()).andReturn();
        return objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
    }

    private ResultActions getResourceByIdRequest(Object id) throws Exception {
        return mockMvc.perform(get("/api/resources/{id}", id).with(asUser()));
    }

    private DetailedResourceResponse getResourceById(UUID id) throws Exception {
        MvcResult result = getResourceByIdRequest(id).andExpect(status().isOk()).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), DetailedResourceResponse.class);
    }

    private ResultActions putResourceRequest(Object id, CreateResourceRequest request) throws Exception {
        return mockMvc.perform(put("/api/resources/{id}", id)
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private DetailedResourceResponse putResource(UUID id, CreateResourceRequest request) throws Exception {
        MvcResult result = putResourceRequest(id, request).andExpect(status().isOk()).andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), DetailedResourceResponse.class);
    }

    private ResultActions patchStatusRequest(UUID id, Boolean active) throws Exception {
        return mockMvc.perform(patch("/api/resources/{id}/status", id)
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateStatusRequest(active))));
    }

    private void patchStatus(UUID id, boolean active) throws Exception {
        patchStatusRequest(id, active).andExpect(status().isOk());
    }
}