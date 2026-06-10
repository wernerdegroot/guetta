package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import nl.wernerdegroot.guetta.core.optics.supporting.StatePair;
import nl.wernerdegroot.guetta.core.optics.supporting.Stateful;
import nl.wernerdegroot.guetta.core.optics.supporting.StatefulPredicate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SetterWithStatefulEffect<Structure, Value> {

    record WithInternalState<S, I>(S userState, I internalState) {
    }


    <State, Result> Stateful<Continuation<Result, Structure>, State> modifyWithEffect(Structure structure, State initialState, BiFunction<Value, State, Stateful<Continuation<Result, Value>, State>> modifierWithEffect);

    default <State, Result> Function<Structure, Stateful<Continuation<Result, Structure>, State>> modifyWithEffect(State initialState, BiFunction<Value, State, Stateful<Continuation<Result, Value>, State>> modifierWithEffect) {
        return structure -> modifyWithEffect(structure, initialState, modifierWithEffect);
    }

    default <State, Result> BiFunction<Structure, State, Stateful<Continuation<Result, Structure>, State>> modifyWithEffect(BiFunction<Value, State, Stateful<Continuation<Result, Value>, State>> modifierWithEffect) {
        return (structure, initialState) -> modifyWithEffect(structure, initialState, modifierWithEffect);
    }

    default <T> SetterWithStatefulEffect<Structure, T> andThen(SetterWithStatefulEffect<Value, T> that) {
        var self = this;
        return new SetterWithStatefulEffect<>() {
            @Override
            public <State, Result> Stateful<Continuation<Result, Structure>, State> modifyWithEffect(Structure structure, State initialState, BiFunction<T, State, Stateful<Continuation<Result, T>, State>> modifierWithEffect) {
                return self.modifyWithEffect(structure, initialState, that.modifyWithEffect(modifierWithEffect));
            }
        };
    }

    /**
     * Creates a {@link SetterWithStatefulEffect} that only modifies the first {@code numberToTake} elements.
     *
     * @param numberToTake the number of elements to modify
     * @return a new {@link SetterWithStatefulEffect}
     */
    default SetterWithStatefulEffect<Structure, Value> take(int numberToTake) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index < numberToTake, index + 1)
        );
    }

    /**
     * Creates a {@link SetterWithStatefulEffect} that only modifies the elements after the first {@code numberToDrop} elements.
     *
     * @param numberToDrop the number of elements to drop
     * @return a new {@link SetterWithStatefulEffect}
     */
    default SetterWithStatefulEffect<Structure, Value> drop(int numberToDrop) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index >= numberToDrop, index + 1)
        );
    }

    /**
     * Creates a {@link SetterWithStatefulEffect} that only modifies the elements as long as the {@code predicate} holds.
     *
     * @param predicate the predicate to check
     * @return a new {@link SetterWithStatefulEffect}
     */
    default SetterWithStatefulEffect<Structure, Value> takeWhile(Predicate<Value> predicate) {
        return filterWithState(
                true,
                (value, stillTaking) -> {
                    if (stillTaking && predicate.test(value)) {
                        return StatePair.of(true, true);
                    } else {
                        return StatePair.of(false, false);
                    }
                }
        );
    }

    /**
     * Creates a {@link SetterWithStatefulEffect} that only modifies the elements once the {@code predicate} no longer holds.
     *
     * @param predicate the predicate to check
     * @return a new {@link SetterWithStatefulEffect}
     */
    default SetterWithStatefulEffect<Structure, Value> dropWhile(Predicate<Value> predicate) {
        return filterWithState(
                true,
                (value, stillDropping) -> {
                    if (stillDropping && predicate.test(value)) {
                        return StatePair.of(false, true);
                    } else {
                        return StatePair.of(true, false);
                    }
                }
        );
    }

    private <InternalState> SetterWithStatefulEffect<Structure, Value> filterWithState(
            InternalState initialInternalState,
            StatefulPredicate<Value, InternalState> step
    ) {
        var self = this;
        return new SetterWithStatefulEffect<>() {
            @Override
            public <State, Result> Stateful<Continuation<Result, Structure>, State> modifyWithEffect(Structure structure, State initialState, BiFunction<Value, State, Stateful<Continuation<Result, Value>, State>> modifier) {
                var result = self.modifyWithEffect(
                        structure,
                        new WithInternalState<>(initialState, initialInternalState),
                        (value, stateWithInternal) -> {
                            StatePair<Boolean, InternalState> next = step.test(value, stateWithInternal.internalState());
                            boolean shouldModify = next.value();
                            InternalState nextInternalState = next.state();

                            if (shouldModify) {
                                return modifier.apply(value, stateWithInternal.userState())
                                        .mapState(nextUserState -> new WithInternalState<>(nextUserState, nextInternalState));
                            } else {
                                return Stateful.of(Continuation.of(value), new WithInternalState<>(stateWithInternal.userState(), nextInternalState));
                            }
                        }
                );

                return Stateful.of(result.value(), result.state().userState());
            }
        };
    }

}
