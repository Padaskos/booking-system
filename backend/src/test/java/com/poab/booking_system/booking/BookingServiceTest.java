package com.poab.booking_system.booking;

import com.poab.booking_system.resource.ResourceEntity;
import com.poab.booking_system.resource.ResourceService;
import com.poab.booking_system.resource.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void bookingFailsWhenResourceIsInactive() {
        UUID resourceID = UUID.randomUUID();

        ResourceEntity inactiveResource = createResource();
        inactiveResource.setActive(false);
        when(resourceService.getResourceById(resourceID)).thenReturn(inactiveResource);

        assertThrows(ResourceNotAvailableException.class, () -> bookingService.createBooking(resourceID));
    }

    @Test
    void bookingFailsWhenRequestedPeriodIsUnavailable() {
        UUID resourceID = UUID.randomUUID();

    }

    private static final String genericName = "Name";
    private static final String genericDescription = "Description";
    private static final String genericLocation = "Location";
    private static final ResourceType genericResourceType = ResourceType.LAPTOP;

    private static ResourceEntity createResource() {
        return new ResourceEntity(genericName, genericDescription, genericLocation, genericResourceType);
    }
}
