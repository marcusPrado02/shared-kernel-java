package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.util.List;

public record Tags(List<BlobTag> values) {
    public static Tags empty(){ return new Tags(List.of()); }
}