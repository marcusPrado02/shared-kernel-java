package com.marcusprado02.sharedkernel.infrastructure.hateoas.example.impl;

/** View-model exemplificativo (compatível com seu ResponseDTO). */
record CustomerRes(String id, long version, boolean deleted, String etag) {}
