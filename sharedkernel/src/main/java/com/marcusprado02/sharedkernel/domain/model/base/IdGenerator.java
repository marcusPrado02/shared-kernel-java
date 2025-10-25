package com.marcusprado02.sharedkernel.domain.model.base;


public interface IdGenerator<ID extends Identifier> {
    ID newId();
}
