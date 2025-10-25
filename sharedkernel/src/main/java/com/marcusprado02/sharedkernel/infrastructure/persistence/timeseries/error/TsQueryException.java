package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.error;

public class TsQueryException extends TsException {
  public TsQueryException(String message) { super(message); }
  public TsQueryException(String message, Throwable cause) { super(message, cause); }
  public TsQueryException(Throwable cause) { super(cause); }
}
