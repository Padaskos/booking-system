package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ResourceService {

    private final ResourceRepository repository;

    ResourceService(
            ResourceRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public void createResource(CreateResourceRequest request) {
        ResourceEntity entity = new ResourceEntity(request.name(), request.description(), request.location(), request.type());
        repository.save(entity);
    }

    public Page<ResourceEntity> getResources(Pageable pageable, ResourceType type, String name, String location) {
        Specification<ResourceEntity> spec = Specification
                .where(ResourceSpecifications.isActive())
                .and(ResourceSpecifications.nameLike(name))
                .and(ResourceSpecifications.hasType(type))
                .and(ResourceSpecifications.locationLike(location));

        return repository.findAll(spec, pageable);
    }

    public DetailedResourceResponse getResourceById(UUID id) {
        return repository.findById(id)
                .map(entity -> new DetailedResourceResponse(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getLocation(),
                        entity.getType(),
                        entity.isActive(),
                        entity.getCreationTimestamp(),
                        entity.getUpdatedTimestamp()
                ))
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }
}
