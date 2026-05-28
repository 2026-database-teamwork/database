package database.assignment.domain.rent.event;

public record RentCompletedEvent (
        String license,
        Long userCouponId
){ }
