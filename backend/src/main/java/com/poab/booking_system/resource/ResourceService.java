package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import jakarta.transaction.Transactional;
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
    void createResource(CreateResourceRequest request) {
        ResourceEntity entity = new ResourceEntity(request.name(), request.description(), request.location(), request.type());
        repository.save(entity);
    }

    @Transactional
    DetailedResourceResponse updateResource(CreateResourceRequest request, UUID id) {
        ResourceEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource id " + id + " is incorrect or resource does not exist"));

        entity.updateDetails(
                request.name(),
                request.description(),
                request.location(),
                request.type()
        );
        repository.saveAndFlush(entity);

        return new DetailedResourceResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getType(),
                entity.isActive(),
                entity.getCreationTimestamp(),
                entity.getUpdatedTimestamp()
        );
    }
}
