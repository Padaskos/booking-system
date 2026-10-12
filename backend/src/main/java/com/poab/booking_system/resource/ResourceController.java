package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private final ResourceService resourceService;

    ResourceController(
            ResourceService resourceService
    ) {
        this.resourceService = resourceService;
    }

    @PostMapping("/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public void createResource(@Valid @RequestBody CreateResourceRequest request) {
        resourceService.createResource(request);
    }

    @PutMapping("/resources/{id}")
    public DetailedResourceResponse updateResource(@Valid @RequestBody CreateResourceRequest request, @PathVariable UUID id) {
        return resourceService.updateResource(request, id);
    }
}
