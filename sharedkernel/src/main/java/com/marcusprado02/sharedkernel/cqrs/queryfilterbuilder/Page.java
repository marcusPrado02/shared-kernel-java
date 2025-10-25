package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;
import java.util.*;

public final class Page {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public final int number;
    public final int size;

    public Page(int number, int size) {
        this.number = Math.max(0, number);
        this.size = Math.min(Math.max(1, size), MAX_SIZE);
    }
}
