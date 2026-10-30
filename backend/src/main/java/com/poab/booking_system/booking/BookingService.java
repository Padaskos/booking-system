package com.poab.booking_system.booking;

import com.poab.booking_system.resource.ResourceEntity;
import com.poab.booking_system.resource.ResourceService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingService {

    private final ResourceService resourceService;

    BookingService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void createBooking(UUID resourceId) {
        ResourceEntity resource = resourceService.getResourceById(resourceId);
        if (!resource.isActive()) {
            throw new ResourceNotAvailableException(resourceId);
        }
    }

}
