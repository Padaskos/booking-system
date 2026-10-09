package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.ResourceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceUnitTest {

    @Mock
    private ResourceRepository repository;

    @InjectMocks
    private ResourceService resourceService;

    private ResourceEntity makeEntity(String name, String description, String location, ResourceType type) {
        return new ResourceEntity(name, description, location, type);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    @Test
    void getResources_shouldReturnAllResults_whenNoFiltersApplied() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP),
                makeEntity("Camera A", "desc", "Gent", ResourceType.CAMERA)
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getResources_shouldReturnFilteredResults_whenTypeProvided() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP)
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), ResourceType.LAPTOP, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).type()).isEqualTo(ResourceType.LAPTOP);
    }

    @Test
    void getResources_shouldReturnFilteredResults_whenNameProvided() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP)
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), null, "laptop", null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Laptop A");
    }

    @Test
    void getResources_shouldReturnFilteredResults_whenLocationProvided() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Camera A", "desc", "Gent", ResourceType.CAMERA)
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), null, null, "gent");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).location()).isEqualTo("Gent");
    }

    @Test
    void getResources_shouldReturnEmpty_whenNoResultsMatch() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), null, "nonexistent", null);

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getResources_shouldMapEntityFieldsToResponseCorrectly() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Laptop A", "A laptop", "Antwerpen", ResourceType.LAPTOP)
        );
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities));

        Page<ResourceResponse> result = resourceService.getResources(pageable(0, 20), null, null, null);

        ResourceResponse response = result.getContent().get(0);
        assertThat(response.name()).isEqualTo("Laptop A");
        assertThat(response.description()).isEqualTo("A laptop");
        assertThat(response.location()).isEqualTo("Antwerpen");
        assertThat(response.type()).isEqualTo(ResourceType.LAPTOP);
    }

    @Test
    void getResources_shouldReturnCorrectPageMetadata() {
        List<ResourceEntity> entities = List.of(
                makeEntity("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP),
                makeEntity("Laptop B", "desc", "Gent", ResourceType.LAPTOP)
        );
        Pageable smallPage = pageable(0, 2);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(entities, smallPage, 5));

        Page<ResourceResponse> result = resourceService.getResources(smallPage, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
}