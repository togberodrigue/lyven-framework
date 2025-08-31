package io.lyven.core.reactive;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for reactive operations in Lyven
 * Provides Angular/RxJS-like operators and helpers
 */
public class ReactiveUtils {

    /**
     * Create an Observable from a CompletableFuture
     */
    public static <T> Observable<T> fromFuture(CompletableFuture<T> future) {
        return Observable.from(Mono.fromFuture(future));
    }

    /**
     * Create an Observable from a supplier (lazy evaluation)
     */
    public static <T> Observable<T> fromSupplier(Supplier<T> supplier) {
        return Observable.from(Mono.fromSupplier(supplier));
    }

    /**
     * Delay execution (like RxJS delay)
     */
    public static <T> Function<Observable<T>, Observable<T>> delay(Duration duration) {
        return obs -> Observable.from(obs.toMono().delayElement(duration));
    }

    /**
     * Timeout operator (like RxJS timeout)
     */
    public static <T> Function<Observable<T>, Observable<T>> timeout(Duration duration) {
        return obs -> Observable.from(obs.toMono().timeout(duration));
    }

    /**
     * Retry operator (like RxJS retry)
     */
    public static <T> Function<Observable<T>, Observable<T>> retry(int times) {
        return obs -> Observable.from(obs.toMono().retry(times));
    }

    /**
     * CatchError operator (like RxJS catchError)
     */
    public static <T> Function<Observable<T>, Observable<T>> catchError(Function<Throwable, Observable<T>> errorHandler) {
        return obs -> Observable.from(
                obs.toMono().onErrorResume(error -> errorHandler.apply(error).toMono())
        );
    }

    /**
     * Combine multiple Observables (like RxJS combineLatest)
     */
    public static <T1, T2, R> Observable<R> combineLatest(
            Observable<T1> obs1,
            Observable<T2> obs2,
            Function<T1, Function<T2, R>> combiner) {
        return Observable.from(
                Mono.zip(obs1.toMono(), obs2.toMono())
                        .map(tuple -> combiner.apply(tuple.getT1()).apply(tuple.getT2()))
        );
    }

    /**
     * Merge multiple Observables into one stream
     */
    @SafeVarargs
    public static <T> ObservableList<T> merge(Observable<T>... observables) {
        Flux<T> merged = Flux.merge(
                Flux.fromArray(observables).map(Observable::toMono)
        );
        return ObservableList.from(merged);
    }

    /**
     * Create an interval Observable (like RxJS interval)
     */
    public static ObservableList<Long> interval(Duration period) {
        return ObservableList.from(Flux.interval(period));
    }

    /**
     * Create a timer Observable (like RxJS timer)
     */
    public static Observable<Long> timer(Duration delay) {
        return Observable.from(Mono.delay(delay).map(i -> 0L));
    }

    /**
     * Convert any value to Observable
     */
    public static <T> Observable<T> just(T value) {
        return Observable.of(value);
    }

    /**
     * Create empty Observable
     */
    public static <T> Observable<T> empty() {
        return Observable.empty();
    }
}