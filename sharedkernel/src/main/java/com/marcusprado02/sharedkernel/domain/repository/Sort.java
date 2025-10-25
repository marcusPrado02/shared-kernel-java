package com.marcusprado02.sharedkernel.domain.repository;

import java.util.List;

public record Sort(String property, Direction direction) {
    public enum Direction { ASC, DESC }
    public static Sort asc(String p)  { return new Sort(p, Direction.ASC); }
    public static Sort desc(String p) { return new Sort(p, Direction.DESC); }
}