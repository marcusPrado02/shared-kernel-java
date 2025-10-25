package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;


/** Link com RFC-6570 template. */
public record TemplateLink(Rel rel, String hrefTemplate, String type, String title, Map<String,Object> meta,
                           List<Action> actions) {}
