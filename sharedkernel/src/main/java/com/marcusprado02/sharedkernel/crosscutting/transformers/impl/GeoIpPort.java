package com.marcusprado02.sharedkernel.crosscutting.transformers.impl;

import java.util.Optional;

public interface GeoIpPort { Optional<String> countryIso(String ip) throws Exception; }
