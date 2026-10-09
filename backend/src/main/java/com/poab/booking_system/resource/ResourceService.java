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
