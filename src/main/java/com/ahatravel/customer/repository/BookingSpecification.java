package com.ahatravel.customer.repository;

import com.ahatravel.customer.entity.Booking;
import com.ahatravel.customer.enums.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class BookingSpecification {

    public static Specification<Booking> forUser(Long userId) {
        return (root, query, cb) ->
                userId == null ? cb.conjunction() : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Booking> hasStatus(BookingStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> pickupAfter(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("pickupDatetime"), from);
    }

    public static Specification<Booking> pickupBefore(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("pickupDatetime"), to);
    }
}
