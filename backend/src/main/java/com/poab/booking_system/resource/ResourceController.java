package com.poab.booking_system.resource;

import com.poab.booking_system.resource.dto.CreateResourceRequest;
import com.poab.booking_system.resource.dto.DetailedResourceResponse;
import com.poab.booking_system.resource.dto.ResourceResponse;
import com.poab.booking_system.resource.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class ResourceController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "location", "type");
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
    public Page<ResourceResponse> getResources(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) ResourceType type,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Integer page
    ) {
        validatePageable(pageable, page);
        return resourceService.getResources(pageable, type, name, location)
                .map(entity -> new ResourceResponse(
                        entity.getId(),
                        entity.getName(),
                        entity.getDescription(),
                        entity.getLocation(),
                        entity.getType()
                ));
    }

    private static void validatePageable(Pageable pageable, Integer page) {
        if (page != null && page < 0) {
            throw new InvalidRequestException("Page must be at least 0");
        }

        if (pageable.getPageSize() > 100) {
            throw new InvalidRequestException("Size cannot exceed 100");
        }

        pageable.getSort().forEach(order -> {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException(
                        "Invalid sort field: " + order.getProperty()
                );
            }
        });
    }

    @GetMapping("/resources/{id}")
    public DetailedResourceResponse getsResourceById(@PathVariable UUID id) {
        return resourceService.getResourceById(id);
    }

    @PatchMapping("/resources/{id}/status")
    public void updateResourceStatusById(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        resourceService.updateStatusById(id, request.status());
    }
}
