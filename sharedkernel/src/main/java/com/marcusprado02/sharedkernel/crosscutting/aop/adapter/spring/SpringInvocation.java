package com.marcusprado02.sharedkernel.crosscutting.aop.adapter.spring;

import com.marcusprado02.sharedkernel.crosscutting.aop.core.Invocation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SpringInvocation implements Invocation {
    private final ProceedingJoinPoint pjp;
    private final Method method;
    private final Class<?> targetClass;

    public SpringInvocation(ProceedingJoinPoint pjp) {
        this.pjp = pjp;
        this.method = ((MethodSignature) pjp.getSignature()).getMethod();
        this.targetClass = (pjp.getTarget() != null ? pjp.getTarget().getClass() : method.getDeclaringClass());
    }

    @Override
    public Object proceed() throws Throwable {
        return pjp.proceed();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> findAnnotation(Class<T> annType) {
        Annotation onMethod = AnnotatedElementUtils.findMergedAnnotation(method, (Class<? extends Annotation>) annType);
        if (onMethod != null) return Optional.of((T) onMethod);
        Annotation onType = AnnotatedElementUtils.findMergedAnnotation(targetClass, (Class<? extends Annotation>) annType);
        return Optional.ofNullable((T) onType);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public String getMethodName() {
        return method.getName();
    }

    @Override
    public String getClassName() {
        return targetClass.getName();
    }

    // ====== MÉTODOS QUE FALTAVAM ======

    @Override
    public Object[] getArguments() {
        // retorna o array do PJP; se quiser proteger contra mutação externa, faça return Arrays.copyOf(...);
        return pjp.getArgs();
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    @Override
    public Object getTarget() {
        return pjp.getTarget();
    }

    @Override
    public Map<String, Object> getAnnotationsByType() {
        // junta anotações do método e da classe (a classe não sobrescreve chaves já presentes do método)
        Map<String, Object> out = new LinkedHashMap<>();
        for (Annotation a : method.getAnnotations()) {
            out.put(a.annotationType().getName(), a);
        }
        for (Annotation a : targetClass.getAnnotations()) {
            out.putIfAbsent(a.annotationType().getName(), a);
        }
        return out;
    }
}
