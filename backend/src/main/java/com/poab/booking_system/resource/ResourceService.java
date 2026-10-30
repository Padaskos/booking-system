package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
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

    public ResourceEntity getResourceById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    @Transactional
    public ResourceEntity updateResource(CreateResourceRequest request, UUID id) {
        ResourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        entity.updateDetails(
                request.name(),
                request.description(),
                request.location(),
                request.type()
        );
        repository.saveAndFlush(entity);
        return entity;
    }

    @Transactional
    public void updateStatusById(UUID id, boolean status) {
        ResourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        entity.setActive(status);
    }
}
