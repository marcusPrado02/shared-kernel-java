package com.marcusprado02.sharedkernel.domain.specification.examples;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.marcusprado02.sharedkernel.domain.repository.Specification;

public final class IsBusinessDaySpec implements Specification<LocalDate> {
    @Override
    public boolean isSatisfiedBy(LocalDate date) {
        var dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }
}