package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.StatePair;
import nl.wernerdegroot.guetta.core.optics.supporting.StatefulPredicate;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;

@FunctionalInterface
public interface Setter<Structure, Value> {

    static <Structure, Value> Setter<Structure, Value> from(Streamer<Structure, Value> streamer, Collector<Value, ?, Structure> collector) {
        return (structure, modifier) -> streamer.stream(structure).map(modifier).collect(collector);
    }

    default Setter<Structure, Value> asSetter() {
        return this;
    }

    default Structure set(Structure structure, Value value) {
        return modify(structure, ignored -> value);
    }

    default UnaryOperator<Structure> set(Value value) {
        return structure -> set(structure, value);
    }

    Structure modify(Structure structure, UnaryOperator<Value> modifier);

    default UnaryOperator<Structure> modify(UnaryOperator<Value> modifier) {
        return structure -> modify(structure, modifier);
    }

    default <T> Setter<Structure, T> andThen(Setter<Value, T> that) {
        return (structure, modifier) -> this.modify(structure, that.modify(modifier));
    }

    default Setter<Structure, Value> filter(Predicate<Value> predicate) {
        return (structure, modifier) -> this.modify(structure, value -> predicate.test(value) ? modifier.apply(value) : value);
    }

    default Setter<Structure, Value> take(int n) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index < n, index + 1)
        );
    }

    default Setter<Structure, Value> takeWhile(Predicate<Value> predicate) {
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

    /**
     * Creates a {@link Setter} that only modifies the elements after the first {@code numberToDrop} elements.
     *
     * @param numberToDrop the number of elements to drop
     * @return a new {@link Setter}
     */
    default Setter<Structure, Value> drop(int numberToDrop) {
        return filterWithState(
                0,
                (value, index) -> StatePair.of(index >= numberToDrop, index + 1)
        );
    }

    /**
     * Creates a {@link Setter} that only modifies the elements once the {@code predicate} no longer holds.
     *
     * @param predicate the predicate to check
     * @return a new {@link Setter}
     */
    default Setter<Structure, Value> dropWhile(Predicate<Value> predicate) {
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

    private <State> Setter<Structure, Value> filterWithState(State initialState, StatefulPredicate<Value, State> step) {
        return (structure, modifier) -> {
            var stateWrapper = new Object() {
                State current = initialState;
            };
            return this.modify(structure, value -> {
                StatePair<Boolean, State> next = step.test(value, stateWrapper.current);
                stateWrapper.current = next.state();
                if (next.value()) {
                    return modifier.apply(value);
                } else {
                    return value;
                }
            });
        };
    }
}
