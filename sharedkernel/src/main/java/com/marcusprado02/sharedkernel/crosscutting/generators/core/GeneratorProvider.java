package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.net.URI;
import java.util.*;

public interface GeneratorProvider {
    boolean supports(URI uri);
    Generator<?> create(URI uri, Map<String,?> defaults);
}