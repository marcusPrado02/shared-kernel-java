package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions;

public class OptimisticLockException extends RuntimeException { public OptimisticLockException(String m){ super(m);} }

