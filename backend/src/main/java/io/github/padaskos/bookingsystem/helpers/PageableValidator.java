package io.github.padaskos.bookingsystem.helpers;

import io.github.padaskos.bookingsystem.resource.InvalidRequestException;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public final class PageableValidator {

    private PageableValidator() {
    }

    public static void validateWithoutSorting(
            Pageable pageable,
            Integer page,
            Integer size,
            String sortingErrorMessage
    ) {
        validatePageAndSize(pageable, page, size);

        if (pageable.getSort().isSorted()) {
            throw new InvalidRequestException(sortingErrorMessage);
        }
    }

    public static void validateWithAllowedSortFields(
            Pageable pageable,
            Integer page,
            Integer size,
            Set<String> allowedSortFields
    ) {
        validatePageAndSize(pageable, page, size);

        pageable.getSort().forEach(order -> {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new InvalidRequestException(
                        "Invalid sort field: " + order.getProperty()
                );
            }
        });
    }

    private static void validatePageAndSize(Pageable pageable, Integer page, Integer size) {
        if (page != null && page < 0) {
            throw new InvalidRequestException("Page must be at least 0");
        }

        if (size != null && size < 1) {
            throw new InvalidRequestException("Size must be at least 1");
        }

        if (pageable.getPageSize() > 100) {
            throw new InvalidRequestException("Size cannot exceed 100");
        }
    }
}