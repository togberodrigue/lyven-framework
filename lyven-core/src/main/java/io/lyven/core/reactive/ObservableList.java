package io.lyven.core.reactive;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Angular-like Observable for collections
 * Wraps Reactor Flux to provide familiar Angular syntax
 */
public record ObservableList<T>(Flux<T> flux) {

    @SafeVarargs
    public static <T> ObservableList<T> of(T... values) {
        return new ObservableList<>(Flux.just(values));
    }

    public static <T> ObservableList<T> fromList(List<T> list) {
        return new ObservableList<>(Flux.fromIterable(list));
    }

    public static <T> ObservableList<T> from(Flux<T> flux) {
        return new ObservableList<>(flux);
    }

    /**
     * Subscribe to the stream
     */
    public void subscribe(Consumer<T> onNext) {
        flux.subscribe(onNext);
    }

    /**
     * Map each element
     */
    public <R> ObservableList<R> map(Function<T, R> mapper) {
        return new ObservableList<>(flux.map(mapper));
    }

    /**
     * Filter elements
     */
    public ObservableList<T> filter(Function<T, Boolean> predicate) {
        return new ObservableList<>(flux.filter(predicate::apply));
    }

    /**
     * Convert to Reactor Flux (internal use)
     */
    public Flux<T> toFlux() {
        return flux;
    }

    /**
     * Collect to List Observable
     */
    public Observable<List<T>> collectToList() {
        return Observable.from(flux.collectList());
    }
}