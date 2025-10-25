package com.marcusprado02.sharedkernel.infrastructure.config.reloadable.adapter.spring;

import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;
import com.marcusprado02.sharedkernel.infrastructure.config.reloadable.api.ReloadableConfig;

public interface FlagsReloadable extends ReloadableConfig<Map<String, FlagDefinition>> {}
