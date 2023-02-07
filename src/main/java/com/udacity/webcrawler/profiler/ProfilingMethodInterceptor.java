package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import static java.time.Duration.between;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    private final Object target;
    private final ProfilingState profilingState;

    // TODO: You will need to add more instance fields and constructor arguments to this class.
    ProfilingMethodInterceptor(Clock clock, ProfilingState profilingState, Object target) {
        this.clock = Objects.requireNonNull(clock);
        this.target = Objects.requireNonNull(target);
        this.profilingState = Objects.requireNonNull(profilingState);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO: This method interceptor should inspect the called method to see if it is a profiled
        //       method. For profiled methods, the interceptor should record the start time, then
        //       invoke the method using the object that is being profiled. Finally, for profiled
        //       methods, the interceptor should record how long the method call took, using the
        //       ProfilingState methods.
        Instant start = null;
        Object result;
        if (method.isAnnotationPresent(Profiled.class)) {
            start = clock.instant();
        }
        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException();
        } finally {
            if (method.isAnnotationPresent(Profiled.class)) {
                Instant end = clock.instant();
                profilingState.record(target.getClass(), method, between(start, end));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilingMethodInterceptor that = (ProfilingMethodInterceptor) o;
        return Objects.equals(clock, that.clock) && Objects.equals(target, that.target) && Objects.equals(profilingState, that.profilingState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clock, target, profilingState);
    }
}
