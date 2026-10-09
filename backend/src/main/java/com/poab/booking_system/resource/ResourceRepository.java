package com.poab.booking_system.resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ResourceRepository extends JpaRepository<ResourceEntity, UUID>,
        JpaSpecificationExecutor<ResourceEntity> {
    }
