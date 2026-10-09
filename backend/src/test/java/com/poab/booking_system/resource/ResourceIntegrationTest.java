
package com.poab.booking_system.resource;

import tools.jackson.databind.ObjectMapper;
import com.poab.booking_system.resource.dto.CreateResourceRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        mockMvc.perform(get("/api/resources"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getResources_shouldFilterByType() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("type", "LAPTOP"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].type").value("LAPTOP"))
                .andExpect(jsonPath("$.content[1].type").value("LAPTOP"));
    }

    @Test
    void getResources_shouldFilterByName_caseInsensitive() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("name", "laptop"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getResources_shouldFilterByLocation() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("location","gent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getResources_shouldFilterByNameAndType_combined() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("name", "Camera")
                        .param("type", "CAMERA"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Camera A"));
    }

    @Test
    void getResources_shouldReturnEmpty_whenNoMatchFound() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("name", "nonexistent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void getResources_shouldRespectPagination() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getResources_shouldNotReturnInactiveResources() throws Exception {
        ResourceEntity inactive = new ResourceEntity("Old Laptop", "desc", "Antwerpen", ResourceType.LAPTOP);
        inactive.setActive(false);
        repository.save(inactive);

        mockMvc.perform(get("/api/resources"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void getResources_shouldReturn400_whenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("page", "-1"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResources_shouldReturn400_whenSizeExceedsLimit() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("size", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResources_shouldReturn400_whenTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("type", "INVALID_TYPE"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}