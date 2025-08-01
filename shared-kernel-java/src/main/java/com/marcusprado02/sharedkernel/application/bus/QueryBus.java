package com.marcusprado02.sharedkernel.application.bus;

import com.marcusprado02.sharedkernel.application.query.Query;

public interface QueryBus {
    <R, Q extends Query<R>> R dispatch(Q query);
}

