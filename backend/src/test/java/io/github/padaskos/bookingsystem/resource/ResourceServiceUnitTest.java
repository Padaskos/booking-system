package io.github.padaskos.bookingsystem.resource;

import io.github.padaskos.bookingsystem.resource.dto.CreateResourceRequest;
import io.github.padaskos.bookingsystem.resource.dto.ResourceNameResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceUnitTest {

    @Mock
    private ResourceRepository repository;

    @InjectMocks
    private ResourceService resourceService;

    @Test
    void createResourceSavesResourceWhenRequestIsValid() {
        CreateResourceRequest request = new CreateResourceRequest(
                "Laptop A",
                "A laptop",
                "Antwerpen",
                ResourceType.LAPTOP
        );

        resourceService.createResource(request);

        ArgumentCaptor<ResourceEntity> captor = ArgumentCaptor.forClass(ResourceEntity.class);

        verify(repository).save(captor.capture());

        ResourceEntity saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Laptop A");
        assertThat(saved.getDescription()).isEqualTo("A laptop");
        assertThat(saved.getLocation()).isEqualTo("Antwerpen");
        assertThat(saved.getType()).isEqualTo(ResourceType.LAPTOP);
    }

    @Test
    void getResourcesReturnsAllActiveResourcesWhenNoFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        ResourceEntity camera = resource("Camera A", "desc", "Gent", ResourceType.CAMERA);

        givenResources(pageable, laptop, camera);

        Page<ResourceEntity> result = resourceService.getResources(pageable, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(laptop, camera);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesReturnsFilteredResourcesWhenTypeProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);

        givenResources(pageable, laptop);

        Page<ResourceEntity> result = resourceService.getResources(pageable, ResourceType.LAPTOP, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getType()).isEqualTo(ResourceType.LAPTOP);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesReturnsFilteredResourcesWhenNameProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);

        givenResources(pageable, laptop);

        Page<ResourceEntity> result = resourceService.getResources(pageable, null, "laptop", null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Laptop A");

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesReturnsFilteredResourcesWhenLocationProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity camera = resource("Camera A", "desc", "Gent", ResourceType.CAMERA);

        givenResources(pageable, camera);

        Page<ResourceEntity> result = resourceService.getResources(pageable, null, null, "gent");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getLocation()).isEqualTo("Gent");

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesReturnsEmptyPageWhenNoResourcesMatch() {
        Pageable pageable = PageRequest.of(0, 20);

        givenResources(pageable);

        Page<ResourceEntity> result = resourceService.getResources(pageable, null, "nonexistent", null);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesKeepsPageMetadata() {
        Pageable pageable = PageRequest.of(1, 2);

        ResourceEntity laptopA = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        ResourceEntity laptopB = resource("Laptop B", "desc", "Gent", ResourceType.LAPTOP);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(laptopA, laptopB), pageable, 5));

        Page<ResourceEntity> result = resourceService.getResources(pageable, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void getResourcesInactiveReturnsAllInactiveResourcesWhenNoFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        ResourceEntity camera = resource("Camera A", "desc", "Gent", ResourceType.CAMERA);

        givenResources(pageable, laptop, camera);

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, null, null, null);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(laptop, camera);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesInactiveReturnsFilteredResourcesWhenTypeProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);

        givenResources(pageable, laptop);

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, ResourceType.LAPTOP, null, null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getType()).isEqualTo(ResourceType.LAPTOP);

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesInactiveReturnsFilteredResourcesWhenNameProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity laptop = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);

        givenResources(pageable, laptop);

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, null, "laptop", null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Laptop A");

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesInactiveReturnsFilteredResourcesWhenLocationProvided() {
        Pageable pageable = PageRequest.of(0, 20);

        ResourceEntity camera = resource("Camera A", "desc", "Gent", ResourceType.CAMERA);

        givenResources(pageable, camera);

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, null, null, "gent");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getLocation()).isEqualTo("Gent");

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesInactiveReturnsEmptyPageWhenNoResourcesMatch() {
        Pageable pageable = PageRequest.of(0, 20);

        givenResources(pageable);

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, null, "nonexistent", null);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();

        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getResourcesInactiveKeepsPageMetadata() {
        Pageable pageable = PageRequest.of(1, 2);

        ResourceEntity laptopA = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        ResourceEntity laptopB = resource("Laptop B", "desc", "Gent", ResourceType.LAPTOP);

        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(laptopA, laptopB), pageable, 5));

        Page<ResourceEntity> result = resourceService.getResourcesInactive(pageable, null, null, null);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    void getResourceByIdReturnsResourceWhenResourceExists() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);

        givenResourceExists(resourceId, resource);

        ResourceEntity result = resourceService.getResourceById(resourceId);

        assertThat(result).isEqualTo(resource);

        verify(repository).findById(resourceId);
    }

    @Test
    void getResourceByIdFailsWhenResourceDoesNotExist() {
        UUID resourceId = UUID.randomUUID();

        givenResourceDoesNotExist(resourceId);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.getResourceById(resourceId));
    }

    @Test
    void isActiveReturnsTrueWhenResourceIsActive() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        resource.setActive(true);

        givenResourceExists(resourceId, resource);

        boolean result = resourceService.isActive(resourceId);

        assertThat(result).isTrue();

        verify(repository).findById(resourceId);
    }

    @Test
    void isActiveReturnsFalseWhenResourceIsInactive() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        resource.setActive(false);

        givenResourceExists(resourceId, resource);

        boolean result = resourceService.isActive(resourceId);

        assertThat(result).isFalse();

        verify(repository).findById(resourceId);
    }

    @Test
    void isActiveFailsWhenResourceDoesNotExist() {
        UUID resourceId = UUID.randomUUID();

        givenResourceDoesNotExist(resourceId);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.isActive(resourceId));
    }

    @Test
    void updateResourceUpdatesAndReturnsResourceWhenResourceExists() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Old Laptop", "Old desc", "Antwerpen", ResourceType.LAPTOP);
        CreateResourceRequest request = new CreateResourceRequest(
                "Updated Camera",
                "Updated desc",
                "Gent",
                ResourceType.CAMERA
        );

        givenResourceExists(resourceId, resource);

        ResourceEntity result = resourceService.updateResource(request, resourceId);

        assertThat(result).isEqualTo(resource);
        assertThat(result.getName()).isEqualTo("Updated Camera");
        assertThat(result.getDescription()).isEqualTo("Updated desc");
        assertThat(result.getLocation()).isEqualTo("Gent");
        assertThat(result.getType()).isEqualTo(ResourceType.CAMERA);

        verify(repository).findById(resourceId);
        verify(repository).saveAndFlush(resource);
    }

    @Test
    void updateResourceFailsWhenResourceDoesNotExist() {
        UUID resourceId = UUID.randomUUID();
        CreateResourceRequest request = new CreateResourceRequest(
                "Updated Camera",
                "Updated desc",
                "Gent",
                ResourceType.CAMERA
        );

        givenResourceDoesNotExist(resourceId);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.updateResource(request, resourceId));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void updateStatusByIdSetsResourceActiveWhenStatusIsTrue() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        resource.setActive(false);

        givenResourceExists(resourceId, resource);

        resourceService.updateStatusById(resourceId, true);

        assertThat(resource.isActive()).isTrue();

        verify(repository).findById(resourceId);
    }

    @Test
    void updateStatusByIdSetsResourceInactiveWhenStatusIsFalse() {
        UUID resourceId = UUID.randomUUID();
        ResourceEntity resource = resource("Laptop A", "desc", "Antwerpen", ResourceType.LAPTOP);
        resource.setActive(true);

        givenResourceExists(resourceId, resource);

        resourceService.updateStatusById(resourceId, false);

        assertThat(resource.isActive()).isFalse();

        verify(repository).findById(resourceId);
    }

    @Test
    void updateStatusByIdFailsWhenResourceDoesNotExist() {
        UUID resourceId = UUID.randomUUID();

        givenResourceDoesNotExist(resourceId);

        assertThrows(ResourceNotFoundException.class, () -> resourceService.updateStatusById(resourceId, false));
    }

    @Test
    void getRequestedResourceNamesByIdReturnsResourceNamesForRequestedIds() {
        UUID laptopId = UUID.randomUUID();
        UUID cameraId = UUID.randomUUID();

        ResourceRepository.ResourceNameView laptop = resourceNameView(laptopId, "Laptop A");
        ResourceRepository.ResourceNameView camera = resourceNameView(cameraId, "Camera A");

        givenResourceNamesForIds(List.of(laptopId, cameraId), laptop, camera);

        List<ResourceNameResponse> result = resourceService.getRequestedResourceNamesById(List.of(laptopId, cameraId));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(laptopId);
        assertThat(result.get(0).name()).isEqualTo("Laptop A");
        assertThat(result.get(1).id()).isEqualTo(cameraId);
        assertThat(result.get(1).name()).isEqualTo("Camera A");

        verify(repository).findAllByIdIn(List.of(laptopId, cameraId));
    }

    @Test
    void getRequestedResourceNamesByIdReturnsEmptyListWhenNoIdsMatch() {
        List<UUID> resourceIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        givenResourceNamesForIds(resourceIds);

        List<ResourceNameResponse> result = resourceService.getRequestedResourceNamesById(resourceIds);

        assertThat(result).isEmpty();

        verify(repository).findAllByIdIn(resourceIds);
    }

    private void givenResources(Pageable pageable, ResourceEntity... resources) {
        when(repository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(resources), pageable, resources.length));
    }

    private void givenResourceExists(UUID resourceId, ResourceEntity resource) {
        when(repository.findById(resourceId)).thenReturn(Optional.of(resource));
    }

    private void givenResourceDoesNotExist(UUID resourceId) {
        when(repository.findById(resourceId)).thenReturn(Optional.empty());
    }

    private void givenResourceNamesForIds(
            Collection<UUID> resourceIds,
            ResourceRepository.ResourceNameView... resources
    ) {
        when(repository.findAllByIdIn(resourceIds)).thenReturn(List.of(resources));
    }

    private static ResourceRepository.ResourceNameView resourceNameView(UUID id, String name) {
        return new ResourceRepository.ResourceNameView() {
            @Override
            public UUID getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    private static ResourceEntity resource(
            String name,
            String description,
            String location,
            ResourceType type
    ) {
        return new ResourceEntity(name, description, location, type);
    }
}