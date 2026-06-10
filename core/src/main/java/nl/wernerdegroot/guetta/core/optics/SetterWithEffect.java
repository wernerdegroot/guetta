package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface SetterWithEffect<Structure, Value> {

    default SetterWithEffect<Structure, Value> asSetterWithEffect() {
        return this;
    }

    default <Result> Continuation<Result, Structure> setWithEffect(Structure structure, Continuation<Result, Value> valueWithEffect) {
        return modifyWithEffect(structure, ignored -> valueWithEffect);
    }

    default <Result> Function<Structure, Continuation<Result, Structure>> setWithEffect(Continuation<Result, Value> valueWithEffect) {
        return structure -> setWithEffect(structure, valueWithEffect);
    }

    /**
     * Most updates are simple: take an {@code A} and return an updated {@code A}.
     * Applying such modifications to every value in a structure produces another
     * structure of the same exact shape.
     * <p>
     * However, not every modification function returns a value directly. Some modifications
     * may fail, some may finish at a later time, and some may produce multiple possible
     * results:
     *
     * <ol>
     *     <li>Modification {@code Function<A, Optional<A>>} - the modification may fail</li>
     *     <li>Modification {@code Function<A, CompletableFuture<A>>} - the modification may produce an {@code A} at a later time</li>
     *     <li>Modification {@code Function<A, Steam<A>>} - the modification may produce multiple results</li>
     * </ol>
     * <p>
     * The important observation is that traversing a structure with such a
     * function still produces the same structure. The only difference is that the
     * entire structure is now wrapped in the corresponding effect (respectively):
     *
     * <ol>
     *     <li>{@code Optional<Structure>}</li>
     *     <li>{@code CompletableFuture<Structure>}</li>
     *     <li>{@code Stream<Structure>}</li>
     * </ol>
     * <p>
     * In other words, {@link SetterWithEffect#modifyWithEffect} turns a function
     * of the form {@code Value -> Effect<Value>} into a function of the form
     * {@code Structure -> Effect<Structure>}.
     * <p>
     * Java has no common abstraction for effects such as {@code Optional},
     * {@code CompletableFuture}, and {@code Stream}. This method therefore uses
     * {@link Continuation} as a common representation for such computations.
     * The traversal code only needs to know how to walk through the structure
     * and rebuild it. It does not need separate implementations for {@code Optional},
     * {@code CompletableFuture}, {@code Stream}, and so on.
     * <p>
     * The resulting continuation can later be executed as an
     * {@code Optional}, {@code CompletableFuture}, {@code Stream}, or any other
     * effect that can be represented by a continuation.
     */
    <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect);

    default <Result> Function<Structure, Continuation<Result, Structure>> modifyWithEffect(Function<Value, Continuation<Result, Value>> modifierWithEffect) {
        return structure -> modifyWithEffect(structure, modifierWithEffect);
    }

    default <Result> Result modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect, Function<Structure, Result> runner) {
        return modifyWithEffect(structure, modifierWithEffect).run(runner);
    }

    default <Result> Function<Structure, Result> modifyWithEffect(Function<Value, Continuation<Result, Value>> modifierWithEffect, Function<Structure, Result> runner) {
        return structure -> modifyWithEffect(structure, modifierWithEffect).run(runner);
    }

    default <T> SetterWithEffect<Structure, T> andThen(SetterWithEffect<Value, T> that) {
        var self = this;
        return new SetterWithEffect<>() {
            @Override
            public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<T, Continuation<Result, T>> modifierWithEffect) {
                return self.modifyWithEffect(structure, that.modifyWithEffect(modifierWithEffect));
            }
        };
    }

    default Structure modifyWithIdentity(Structure structure, UnaryOperator<Value> modifier) {
        return modifyWithEffect(structure, modifier.andThen(Continuation::identity), Function.identity());
    }

    default Structure modifyWithNullable(Structure structure, UnaryOperator<Value> modifierWithNullable) {
        return modifyWithEffect(structure, modifierWithNullable.andThen(Continuation::nullable), Function.identity());
    }

    default Optional<Structure> modifyWithOptional(Structure structure, Function<Value, Optional<Value>> modifierWithOptional) {
        return modifyWithEffect(structure, modifierWithOptional.andThen(Continuation::optional), Optional::of);
    }

    default CompletableFuture<Structure> modifyWithCompletableFuture(Structure structure, Function<Value, CompletableFuture<Value>> modifierWithCompletableFuture) {
        return modifyWithEffect(structure, modifierWithCompletableFuture.andThen(Continuation::completableFuture), CompletableFuture::completedFuture);
    }

    default Stream<Structure> modifyWithStream(Structure structure, Function<Value, Stream<Value>> modifierWithStream) {
        return modifyWithEffect(structure, modifierWithStream.andThen(Continuation::stream), Stream::of);
    }

    default List<Structure> modifyWithList(Structure structure, Function<Value, List<Value>> modifierWithList) {
        return modifyWithEffect(structure, modifierWithList.andThen(Continuation::list), List::of);
    }

    default SetterWithEffect<Structure, Value> filter(Predicate<Value> predicate) {
        var self = this;
        return new SetterWithEffect<>() {
            @Override
            public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect) {
                return self.modifyWithEffect(structure, value -> predicate.test(value) ? modifierWithEffect.apply(value) : Continuation.of(value));
            }
        };
    }

    /**
     * Creates a new {@link SetterWithEffect} that only modifies the first {@code n} values.
     * All values encountered after the first {@code n} values remain unchanged.
     *
     * @param n The maximum number of elements to modify.
     * @return A new {@link SetterWithEffect} that limits modifications to the first {@code n} elements.
     */
    default SetterWithEffect<Structure, Value> take(int n) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index < n, index + 1)
        );
    }

    /**
     * Creates a new {@link SetterWithEffect} that only modifies elements while the provided
     * {@link Predicate} is {@code true}. As soon as an element is encountered for which
     * the predicate is {@code false}, all subsequent elements are left unchanged.
     *
     * @param predicate The predicate to check for each element.
     * @return A new {@link SetterWithEffect} that modifies elements until the predicate fails for the first time.
     */
    default SetterWithEffect<Structure, Value> takeWhile(Predicate<Value> predicate) {
        return filterWithState(
                true,
                (value, taking) -> {
                    if (taking && predicate.test(value)) {
                        return StatePair.of(true, true);
                    } else {
                        return StatePair.of(false, false);
                    }
                }
        );
    }

    private <State> SetterWithEffect<Structure, Value> filterWithState(
            State initialState,
            StatefulPredicate<Value, State> step
    ) {
        SetterWithEffect<Structure, Value> source = this;
        return new SetterWithEffect<Structure, Value>() {
            @Override
            public <Result> Continuation<Result, Structure> modifyWithEffect(
                    Structure structure,
                    Function<Value, Continuation<Result, Value>> modifierWithEffect
            ) {
                return outerRunner -> {
                    Continuation<StateReader<Result, State>, Structure> inner =
                            source.modifyWithEffect(
                                    structure,
                                    original -> innerRunner -> state -> {
                                        StatePair<Boolean, State> next = step.test(original, state);
                                        boolean shouldModify = next.value();
                                        State nextState = next.state();

                                        if (!shouldModify) {
                                            return innerRunner.apply(original).run(nextState);
                                        }

                                        Continuation<Result, Value> modifiedWithEffect = modifierWithEffect.apply(original);
                                        return modifiedWithEffect.run(modified ->
                                                innerRunner.apply(modified).run(nextState)
                                        );
                                    }
                            );

                    StateReader<Result, State> statefulResult = inner.run(modifiedStructure ->
                            state -> outerRunner.apply(modifiedStructure)
                    );

                    return statefulResult.run(initialState);
                };
            }
        };
    }
}
