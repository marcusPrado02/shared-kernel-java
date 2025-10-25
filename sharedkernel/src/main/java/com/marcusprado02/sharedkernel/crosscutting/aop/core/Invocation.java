package com.marcusprado02.sharedkernel.crosscutting.aop.core;

import java.util.Map;
import java.util.Optional;

public interface Invocation {
    String getMethodName();
    String getClassName();
    Class<?> getDeclaringClass();
    Object[] getArguments();
    Class<?>[] getParameterTypes();
    Object getTarget();
    Map<String, Object> getAnnotationsByType(); // chave = FQN anotação
    <T> Optional<T> findAnnotation(Class<T> annType);
    Object proceed() throws Throwable;
}
