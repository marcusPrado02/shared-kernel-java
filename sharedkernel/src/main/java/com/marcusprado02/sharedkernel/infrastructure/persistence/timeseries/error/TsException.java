package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.error;

public class TsException extends RuntimeException {
  public TsException(String message) { super(message); }
  public TsException(String message, Throwable cause) { super(message, cause); }
  public TsException(Throwable cause) { super(cause); }
}
