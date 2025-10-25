package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model;

public sealed interface FieldValue permits FieldValue.Bool, FieldValue.Num, FieldValue.Str {
  record Bool(boolean v) implements FieldValue {}
  record Num(double v)    implements FieldValue {}
  record Str(String v)    implements FieldValue {}
}