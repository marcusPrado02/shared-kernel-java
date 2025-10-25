package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.error;

public class TsWriteException extends TsException {
  public TsWriteException(String message) { super(message); }
  public TsWriteException(String message, Throwable cause) { super(message, cause); }
  public TsWriteException(Throwable cause) { super(cause); }
}