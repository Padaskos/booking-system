
package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.ResourceResponse;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                new ResourceEntity("Laptop A", "A laptop", "Antwerpen", ResourceType.LAPTOP),
                new ResourceEntity("Laptop B", "Another laptop", "Gent", ResourceType.LAPTOP),
                new ResourceEntity("Camera A", "A camera", "Brussel", ResourceType.CAMERA)
        ));
    }

    private record PageResponse<T> (
            List<T> content,
            long totalElements,
            int totalPages
    ) {}

    private PageResponse<ResourceResponse> readResourcePage(MvcResult result) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {}
        );
    }

    @Test
    void createResource_shouldReturn201_whenRequestIsValid() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest("Projector", "A projector", "Leuven", ResourceType.CAMERA);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void createResource_shouldReturn400_whenNameIsMissing() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest(null, "desc", "Gent", ResourceType.LAPTOP);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void createResource_shouldReturn400_whenTypeIsMissing() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest("Projector", "desc", "Gent", null);

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
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
                        .param("location","gent"))
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
    void getResources_shouldNotReturnInactiveResources()
            throws Exception {

        ResourceEntity inactive = new ResourceEntity(
                "Old Laptop",
                "Description",
                "Antwerpen",
                ResourceType.LAPTOP
        );
        inactive.setActive(false);
        repository.save(inactive);

        MvcResult result = mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andReturn();

        PageResponse<ResourceResponse> response = readResourcePage(result);

        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.content())
                .extracting(ResourceResponse::name)
                .doesNotContain("Old Laptop");
    }

    @Test
    void getResources_shouldSortByNameAscending()
            throws Exception {

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
    void getResources_shouldSortByLocationDescending()
            throws Exception {

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

    @Test
    void getResources_shouldReturn400_whenPageIsNegative()
            throws Exception {

        mockMvc.perform(get("/api/resources")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());
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
}