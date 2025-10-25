package com.marcusprado02.sharedkernel.infrastructure.secrets;


import java.util.*;

public record SecretMetadata(
        SecretRef ref, String backend, String etag, List<SecretVersion> versions, Map<String,String> attributes
) {}
