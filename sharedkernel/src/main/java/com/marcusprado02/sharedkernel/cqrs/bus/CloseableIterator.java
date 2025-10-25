package com.marcusprado02.sharedkernel.cqrs.bus;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** Iterator que também pode (e deve) ser fechado após o uso. */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
  @Override void close();

  /** Utilitário: iterador vazio e fechável. */
  static <T> CloseableIterator<T> empty() {
    return new CloseableIterator<>() {
      @Override public boolean hasNext() { return false; }
      @Override public T next() { throw new NoSuchElementException(); }
      @Override public void close() { /* no-op */ }
    };
  }
}