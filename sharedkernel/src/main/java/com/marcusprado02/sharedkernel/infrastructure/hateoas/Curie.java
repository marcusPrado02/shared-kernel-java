package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;

/** CURIE compacta: ex.: "fs:customer" -> https://api.example.com/rels/customer */
public record Curie(String prefix, URI hrefTemplate) {}
