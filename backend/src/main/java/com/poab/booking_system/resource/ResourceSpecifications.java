package com.poab.booking_system.resource;

import org.springframework.data.jpa.domain.Specification;

public class ResourceSpecifications {
    public static Specification<ResourceEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    public static Specification<ResourceEntity> nameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<ResourceEntity> hasType(ResourceType type) {
        return (root, query, cb) -> {
            if (type == null) return null;
            return cb.equal(root.get("type"), type);
        };
    }

    public static Specification<ResourceEntity> locationLike(String location) {
        return ( root, query, cb) -> {
            if (location == null || location.isBlank()) return null;
            return cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
        };
    }
}
