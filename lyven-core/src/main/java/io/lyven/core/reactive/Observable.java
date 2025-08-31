package io.lyven.core.reactive;


import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Angular-like Observable for Lyven
 * Wraps Reactor Mono/Flux to provide familiar Angular syntax
 */
public record Observable<T>(Mono<T> mono) {

    public static <T> Observable<T> of(T value) {
        return new Observable<>(Mono.just(value));
    }

    public static <T> Observable<T> empty() {
        return new Observable<>(Mono.empty());
    }

    public static <T> Observable<T> from(Mono<T> mono) {
        return new Observable<>(mono);
    }

    /**
     * Angular-like subscribe method
     */
    public void subscribe(Consumer<T> onNext) {
        mono.subscribe(onNext);
    }

    public void subscribe(Consumer<T> onNext, Consumer<Throwable> onError) {
        mono.subscribe(onNext, onError);
    }

    /**
     * Angular-like pipe method for transformations
     */
    public <R> Observable<R> pipe(Function<Observable<T>, Observable<R>> operator) {
        return operator.apply(this);
    }

    /**
     * Map transformation (like RxJS map)
     */
    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<>(mono.map(mapper));
    }

    /**
     * FlatMap transformation (like RxJS switchMap)
     */
    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<>(mono.flatMap(t -> mapper.apply(t).toMono()));
    }

    /**
     * Convert to Reactor Mono (internal use)
     */
    public Mono<T> toMono() {
        return mono;
    }
}