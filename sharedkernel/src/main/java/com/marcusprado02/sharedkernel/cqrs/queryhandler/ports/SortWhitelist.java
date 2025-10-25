package com.marcusprado02.sharedkernel.cqrs.queryhandler.ports;

import java.util.List;

public interface SortWhitelist {
    List<String> allowedFields();
}
