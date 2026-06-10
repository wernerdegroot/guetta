package nl.wernerdegroot.guetta.core.optics.supporting;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

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

    static <Result, Value> Continuation<Result, Value> identity(Value source) {
        return runner -> runner.apply(source);
    }

    static <Result, Value> Continuation<Result, Value> nullable(Value source) {
        return runner -> {
            // Short-circuit. If `source` is `null`, do not
            // continue with the rest of the computation.
            if (source == null) {
                return null;
            }

            return runner.apply(source);
        };
    }

    static <Result1, Result2, Value> Continuation<Continuation<Result1, Optional<Result2>>, Value> optionalT(Continuation<Result1, Optional<Value>> source) {
        return (valueToInner ->
                (optionalResult2ToResult1 ->
                        source.run(optionalValue ->
                                optionalValue
                                        .map(value ->
                                                valueToInner
                                                        .apply(value)
                                                        .run(optionalResult2ToResult1)
                                        )
                                        .orElseGet(() ->
                                                optionalResult2ToResult1.apply(Optional.empty())
                                        )
                        )
                )
        );
    }

    static <Result, Value> Continuation<Optional<Result>, Value> optional(Optional<Value> source) {
        return source::flatMap;
    }

    static <Result, Value> Continuation<CompletableFuture<Result>, Value> completableFuture(CompletableFuture<Value> source) {
        return source::thenCompose;
    }

    static <Result, Value> Continuation<Stream<Result>, Value> stream(Stream<Value> source) {
        return source::flatMap;
    }

    static <Result, Value> Continuation<List<Result>, Value> list(List<Value> source) {
        return runner -> source.stream().map(runner).flatMap(List::stream).toList();
    }

    static <Result1, Result2, Value> Continuation<Continuation<List<Result1>, Optional<Result2>>, Value> listOfOptional(List<Optional<Value>> source) {
        return outerRunner -> {
            return innerRunner -> {
                return source.stream().map(optionalValue -> optionalValue.map(value -> outerRunner.apply(value).run(innerRunner)).orElseGet(() -> innerRunner.apply(Optional.empty()))).flatMap(List::stream).toList();
            };
        };
    }

    static <Value, State, Result> Continuation<StatefulOperation<Result, State>, Value> statefulOperation(StatefulOperation<Value, State> source) {
        return source::flatMap;
    }

    static <Value, State, Result> Continuation<StatefulOperation<Result, State>, Value> statefulOperation2(StatefulOperation<Value, State> source) {
        return runner -> state -> {
            var result = source.run(state);
            var that = runner.apply(result.value());
            return that.run(result.state());
        };
    }

    static <Value, State, Result1, Result2, R> Continuation<Continuation<StatefulOperation<Result1, State>, Continuation<R, Result2>>, Value> statefulOperation3(StatefulOperation<Continuation<R, Value>, State> source) {
        return outerRunner -> {
            return innerRunner -> {
                return state -> {
                    var result = source.run(state);
                    var continuation = result.value();
                    var nextState = result.state();

                    var what = continuation.run(value -> {
                        var nextContinuation = outerRunner.apply(value);
                        var huh = nextContinuation.run(nextNextContinuation -> {
                            var noop = innerRunner.apply(nextNextContinuation).run(nextState);
                            var nextResult = noop.value();
                            var nextNextState = noop.state();
                            return moreState -> StatePair.of(nextResult, moreState);
                        }).run(nextState);
                        return null;
                    });
                    return null;
                };
            };
        };
    }

    static <Result, State, Value> Continuation<StateReader<Result, State>, Value> statefulContinuation(StatefulOperation<Continuation<StateReader<Result, State>, Value>, State> source) {
        return runner -> state -> {
            var statefulContinuation = source.run(state);
            var continuation = statefulContinuation.value();
            var nextState = statefulContinuation.state();

            return continuation.run(runner).run(nextState);
        };
    }

}
