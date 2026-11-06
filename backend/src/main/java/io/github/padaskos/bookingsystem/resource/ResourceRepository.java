package io.github.padaskos.bookingsystem.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<ResourceEntity, UUID>, JpaSpecificationExecutor<ResourceEntity> {
    List<ResourceNameView> findAllByIdIn(Collection<UUID> ids);

    interface ResourceNameView {
        UUID getId();
        String getName();
    }
}
