package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.ResourceResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/resources")
    public ResponseEntity<Page<ResourceResponse>> getResources(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ResourceType type,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String location
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRequestException("Page must be >= 0 and size must be between 1 and 100");
        }
        Pageable pageable = PageRequest.of(page, size);
        return resourceService.getResources(pageable, type, name, location);
    }
}
