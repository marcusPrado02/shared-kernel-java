package com.marcusprado02.sharedkernel.cqrs.handler.tx;

import com.marcusprado02.sharedkernel.cqrs.handler.UnitOfWork;

public interface TransactionManager {
    UnitOfWork newUnitOfWork();
}