package com.marcusprado02.sharedkernel.domain.specification.examples;


import java.time.LocalDate;

import com.marcusprado02.sharedkernel.domain.repository.Specification;
import com.marcusprado02.sharedkernel.domain.specification.examples.OverlappingBookingSpec.Booking;

public final class OverlappingBookingSpec implements Specification<Booking> {
    private final LocalDate start;
    private final LocalDate end;

    public OverlappingBookingSpec(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean isSatisfiedBy(Booking booking) {
        return !(booking.end.isBefore(start) || booking.start.isAfter(end));
    }

    public record Booking(String id, LocalDate start, LocalDate end) {}

}

