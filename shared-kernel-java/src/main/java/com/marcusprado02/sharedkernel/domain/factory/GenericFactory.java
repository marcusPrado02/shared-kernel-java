package com.marcusprado02.sharedkernel.domain.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.marcusprado02.sharedkernel.domain.aggregateroot.AbstractAggregateRoot;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

/**
 * Fábrica genérica e extensível de instâncias de domínio (com suporte a static factory,
 * construtores, ModelMapper e eventos).
 */
@Slf4j
@Component
public class GenericFactory {

    /* ---------- dependências externas ---------- */
    private final ApplicationContext ctx;
    private final ApplicationEventPublisher publisher;
    private final ModelMapper mapper;
    private final Validator validator;

    /* ---------- caches de conversores e reflection ---------- */
    private final Map<Key, Function<Object, Object>> converters = new ConcurrentHashMap<>();
    private final Map<MemberKey, Executable> reflectionCache = new ConcurrentHashMap<>();

    public GenericFactory(ApplicationContext ctx, ApplicationEventPublisher publisher,
            ModelMapper mapper, Validator validator) {
        this.ctx = ctx;
        this.publisher = publisher;
        this.mapper = mapper;
        this.validator = validator;
    }

    /*
     * ================================================================================== API
     * PÚBLICA ==================================================================================
     */

    public <T> T create(Class<T> target) {
        return createFrom(null, target);
    }

    @SuppressWarnings("unchecked")
    public <S, T> T createFrom(S source, Class<T> target) {
        Objects.requireNonNull(target, "Target class must not be null");

        Key key = new Key(source == null ? Void.class : source.getClass(), target);

        // 1. Conversor registrado manualmente
        if (converters.containsKey(key)) {
            log.debug("Usando conversor registrado para {} → {}", key.src().getSimpleName(),
                    key.dst().getSimpleName());
            return (T) converters.get(key).apply(source);
        }

        // 2. Bean gerenciado pelo Spring
        if (ctx != null && ctx.getBeanNamesForType(target).length > 0) {
            log.debug("Instância encontrada no contexto Spring para {}", target.getSimpleName());
            return ctx.getBean(target);
        }

        // 3. Static factory from(S)
        if (source != null) {
            Executable m = reflectMethod(target, "from", source.getClass());
            if (m instanceof Method method)
                return execute(method, null, source);
        }

        // 4. Static factory create()
        Executable sc = reflectMethod(target, "create");
        if (sc instanceof Method staticFactory)
            return execute(staticFactory, null);

        // 5. Construtor com S
        if (source != null) {
            Executable c1 = reflectConstructor(target, source.getClass());
            if (c1 instanceof Constructor<?> ctor)
                return execute(ctor, source);
        }

        // 6. No-arg constructor
        Executable c0 = reflectConstructor(target);
        if (c0 instanceof Constructor<?> ctor0)
            return execute(ctor0);

        // 7. Mapper fallback
        if (source != null) {
            log.debug("Usando fallback com ModelMapper para instanciar {} a partir de {}",
                    target.getSimpleName(), source.getClass().getSimpleName());
            return mapper.map(source, target);
        }

        throw new IllegalStateException("Não foi possível instanciar " + target.getName());
    }

    public <S, T> GenericFactory register(Class<S> src, Class<T> dst, Function<S, T> fn) {
        converters.put(new Key(src, dst), (Function<Object, Object>) fn);
        return this;
    }

    /*
     * ================================================================================== REFLECTION
     * & EXECUÇÃO ==================================================================================
     */

    @SuppressWarnings("unchecked")
    private <T> T execute(Executable exec, Object... args) {
        try {
            exec.setAccessible(true);
            Object instance = (exec instanceof Constructor<?> c) ? c.newInstance(args)
                    : ((Method) exec).invoke(null, args);

            publishEvents(instance);
            validate(instance);

            return (T) instance;

        } catch (InvocationTargetException e) {
            log.error("Erro ao instanciar via invocação refletida: {}",
                    e.getTargetException().getMessage(), e.getTargetException());
            throw new RuntimeException(e.getTargetException());
        } catch (ReflectiveOperationException e) {
            log.error("Erro de reflexão ao instanciar: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao instanciar via GenericFactory: " + e.getMessage(),
                    e);
        }
    }

    private Executable reflectConstructor(Class<?> target, Class<?>... paramTypes) {
        return reflectionCache.computeIfAbsent(new MemberKey(target, "<ctor>", paramTypes), k -> {
            try {
                return target.getDeclaredConstructor(paramTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
    }

    private Executable reflectMethod(Class<?> target, String methodName, Class<?>... paramTypes) {
        return reflectionCache.computeIfAbsent(new MemberKey(target, methodName, paramTypes), k -> {
            try {
                return target.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
    }

    /*
     * ================================================================================== DOMÍNIO &
     * VALIDAÇÃO ==================================================================================
     */

    private void publishEvents(Object instance) {
        if (instance instanceof AbstractAggregateRoot<?> aggregate) {
            aggregate.domainEvents().forEach(event -> {
                log.debug("Publicando evento: {}", event.getClass().getSimpleName());
                publisher.publishEvent(event);
            });
            aggregate.clearDomainEvents();
        }
    }

    private void validate(Object instance) {
        Set<ConstraintViolation<Object>> violations = validator.validate(instance);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(
                    "Validação falhou para " + instance.getClass().getSimpleName(), violations);
        }
    }

    /*
     * ================================================================================== SUPPORT
     * RECORDS ==================================================================================
     */

    private record Key(Class<?> src, Class<?> dst) {
    }
    private record MemberKey(Class<?> type, String name, Class<?>[] params) {
    }
}
