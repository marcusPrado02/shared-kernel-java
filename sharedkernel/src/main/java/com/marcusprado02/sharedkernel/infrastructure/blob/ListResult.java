package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.util.List;

public record ListResult(List<String> keys, String nextCursor, boolean truncated){}

