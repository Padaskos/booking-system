package io.github.padaskos.bookingsystem.resource;

import io.github.padaskos.bookingsystem.helpers.PageableValidator;
import io.github.padaskos.bookingsystem.resource.dto.CreateResourceRequest;
import io.github.padaskos.bookingsystem.resource.dto.DetailedResourceResponse;
import io.github.padaskos.bookingsystem.resource.dto.ResourceResponse;
import io.github.padaskos.bookingsystem.resource.dto.UpdateStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedResourceResponse createResource(@Valid @RequestBody CreateResourceRequest request) {
        return DetailedResourceResponse.fromEntity(resourceService.createResource(request));
    }

    @GetMapping("/resources")
    public Page<ResourceResponse> getResources(
        @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
        @RequestParam(required = false) ResourceType type,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        PageableValidator.validateWithAllowedSortFields(pageable, page, size, ALLOWED_SORT_FIELDS);
        return resourceService.getResources(pageable, type, name, location)
                .map(ResourceResponse::fromEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/resources/inactive")
    public Page<ResourceResponse> getResourcesInactive(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        PageableValidator.validateWithAllowedSortFields(pageable, page, size, ALLOWED_SORT_FIELDS);
        return resourceService.getResourcesInactive(pageable, type, name, location)
                .map(ResourceResponse::fromEntity);
    }

    @GetMapping("/resource/{id}")
    public DetailedResourceResponse getResourceById(@PathVariable UUID id) {
        return DetailedResourceResponse.fromEntity(resourceService.getResourceById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/resource/{id}")
    public DetailedResourceResponse updateResource(@Valid @RequestBody CreateResourceRequest request, @PathVariable UUID id) {
        return DetailedResourceResponse.fromEntity(resourceService.updateResource(request, id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/resource/{id}/status")
    public void updateResourceStatusById(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        resourceService.updateStatusById(id, request.status());
    }

}
