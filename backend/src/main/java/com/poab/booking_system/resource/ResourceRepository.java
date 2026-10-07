package com.poab.booking_system.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<ResourceEntity, UUID> {

}
