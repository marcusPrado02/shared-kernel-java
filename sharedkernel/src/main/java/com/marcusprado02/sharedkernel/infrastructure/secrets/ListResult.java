package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.util.List;

public record ListResult(List<SecretId> items, String nextCursor, boolean truncated){}
