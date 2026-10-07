package com.poab.booking_system.resource;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resource")
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    private String description;

    private String location;

    @Column(name = "resource_type")
    @Enumerated(EnumType.STRING)
    private ResourceType type;

    @Column(name = "is_active")
    private boolean isActive;

    @UpdateTimestamp
    @Column(name = "updated_timestamp")
    private Instant updatedTimestamp;

    @CreationTimestamp
    @Column(name = "creation_timestamp")
    private Instant creationTimestamp;

    public ResourceEntity() {
    }

    public ResourceEntity(
            String name,
            String description,
            String location,
            ResourceType type
    ) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.type = type;
        this.isActive = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getUpdatedTimestamp() {
        return updatedTimestamp;
    }
    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
