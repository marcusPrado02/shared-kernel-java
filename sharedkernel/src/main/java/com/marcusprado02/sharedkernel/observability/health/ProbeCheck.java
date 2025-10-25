package com.marcusprado02.sharedkernel.observability.health;

public interface ProbeCheck {
    ProbeResult check();
    String name();
}
