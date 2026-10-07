package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}
