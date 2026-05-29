package nl.wernerdegroot.guetta.core.optics.supporting;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@FunctionalInterface
public interface Continuation<Result, Value> {

    static <Result, Value> Continuation<Result, Value> of(Value value) {
        return runner -> runner.apply(value);
    }

    Result run(Function<? super Value, ? extends Result> runner);

    default <T> Continuation<Result, T> map(Function<? super Value, ? extends T> fn) {
        return runner -> this.run(value -> runner.apply(fn.apply(value)));
    }

    default <T> Continuation<Result, T> flatMap(Function<? super Value, ? extends Continuation<Result, T>> fn) {
        return runner -> this.run(value -> fn.apply(value).run(runner));
    }

    static <Structure, Value> Function<Value, Continuation<Optional<Structure>, Value>> optional(Function<Value, Optional<Value>> modifierWithEffect) {
        return value -> runner -> modifierWithEffect.apply(value).flatMap(runner);
    }

    static <Structure, Value> Function<Value, Continuation<CompletableFuture<Structure>, Value>> completableFuture(Function<Value, CompletableFuture<Value>> modifierWithEffect) {
        return value -> runner -> modifierWithEffect.apply(value).thenCompose(runner);
    }

    static <Structure, Value> Function<Value, Continuation<List<Structure>, Value>> list(Function<Value, List<Value>> modifierWithEffect) {
        return value -> runner -> modifierWithEffect.apply(value).stream().map(runner).flatMap(List::stream).toList();
    }
}
