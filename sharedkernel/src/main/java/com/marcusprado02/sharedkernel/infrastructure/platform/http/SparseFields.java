package com.marcusprado02.sharedkernel.infrastructure.platform.http;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.http.converter.json.MappingJacksonValue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Requer @JsonFilter("sparseFilter") nos DTOs suportados. */
public class SparseFields {
    public static MappingJacksonValue filter(Object body, String fieldsCsv) {
        var mv = new MappingJacksonValue(body);
        if (fieldsCsv == null || fieldsCsv.isBlank()) return mv;

        Set<String> fields = Arrays.stream(fieldsCsv.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        var fp = new SimpleFilterProvider()
                .addFilter("sparseFilter", SimpleBeanPropertyFilter.filterOutAllExcept(fields));

        mv.setFilters(fp);
        return mv;
    }
}
