package com.marcusprado02.sharedkernel.infrastructure.maptile.api;

import com.marcusprado02.sharedkernel.infrastructure.maptile.model.StyleRef;

public interface StyleRegistry {
    StyleRef resolve(String styleId);
}
