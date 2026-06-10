package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.StatePair;
import nl.wernerdegroot.guetta.core.optics.supporting.Stateful;
import nl.wernerdegroot.guetta.core.optics.supporting.StatefulPredicate;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface SetterWithState<Structure, Value> {

    record WithInternalState<S, I>(S userState, I internalState) {
    }


    default SetterWithState<Structure, Value> asSetterWithState() {
        return this;
    }

    <State> Stateful<Structure, State> modifyWithState(Structure structure, State initialState, BiFunction<Value, State, Stateful<Value, State>> modifierWithState);

    default <State> Function<Structure, Stateful<Structure, State>> modifyWithState(State initialState, BiFunction<Value, State, Stateful<Value, State>> modifierWithState) {
        return structure -> modifyWithState(structure, initialState, modifierWithState);
    }

    default <State> BiFunction<Structure, State, Stateful<Structure, State>> modifyWithState(BiFunction<Value, State, Stateful<Value, State>> modifierWithState) {
        return (structure, initialState) -> modifyWithState(structure, initialState, modifierWithState);
    }

    default <T> SetterWithState<Structure, T> andThen(SetterWithState<Value, T> that) {
        var self = this;
        return new SetterWithState<>() {
            @Override
            public <State> Stateful<Structure, State> modifyWithState(Structure structure, State initialState, BiFunction<T, State, Stateful<T, State>> modifierWithState) {
                return self.modifyWithState(structure, initialState, that.modifyWithState(modifierWithState));
            }
        };
    }

    default SetterWithState<Structure, Value> filter(Predicate<Value> predicate) {
        var self = this;
        return new SetterWithState<>() {
            @Override
            public <State> Stateful<Structure, State> modifyWithState(Structure structure, State initialState, BiFunction<Value, State, Stateful<Value, State>> modifierWithState) {
                return self.modifyWithState(structure, initialState, (value, state) -> predicate.test(value) ? modifierWithState.apply(value, state) : Stateful.of(value, state));
            }
        };
    }

    /**
     * Creates a {@link SetterWithState} that only modifies the first {@code numberToTake} elements.
     *
     * @param numberToTake the number of elements to modify
     * @return a new {@link SetterWithState}
     */
    default SetterWithState<Structure, Value> take(int numberToTake) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index < numberToTake, index + 1)
        );
    }

    /**
     * Creates a {@link SetterWithState} that only modifies the elements after the first {@code numberToDrop} elements.
     *
     * @param numberToDrop the number of elements to drop
     * @return a new {@link SetterWithState}
     */
    default SetterWithState<Structure, Value> drop(int numberToDrop) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index >= numberToDrop, index + 1)
        );
    }

    /**
     * Creates a {@link SetterWithState} that only modifies the elements as long as the {@code predicate} holds.
     *
     * @param predicate the predicate to check
     * @return a new {@link SetterWithState}
     */
    default SetterWithState<Structure, Value> takeWhile(Predicate<Value> predicate) {
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
     * Creates a {@link SetterWithState} that only modifies the elements once the {@code predicate} no longer holds.
     *
     * @param predicate the predicate to check
     * @return a new {@link SetterWithState}
     */
    default SetterWithState<Structure, Value> dropWhile(Predicate<Value> predicate) {
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

    private <InternalState> SetterWithState<Structure, Value> filterWithState(
            InternalState initialInternalState,
            StatefulPredicate<Value, InternalState> step
    ) {
        var self = this;
        return new SetterWithState<>() {
            @Override
            public <State> Stateful<Structure, State> modifyWithState(Structure structure, State initialState, BiFunction<Value, State, Stateful<Value, State>> modifier) {
                var result = self.modifyWithState(
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
                                return Stateful.of(value, new WithInternalState<>(stateWithInternal.userState(), nextInternalState));
                            }
                        }
                );

                return Stateful.of(result.value(), result.state().userState());
            }
        };
    }

    default Structure modifyWithIndex(Structure structure, BiFunction<Value, Integer, Value> modifierWithIndex) {
        return modifyWithState(structure, 0, (value, index) -> Stateful.of(modifierWithIndex.apply(value, index), index + 1)).value();
    }
}
