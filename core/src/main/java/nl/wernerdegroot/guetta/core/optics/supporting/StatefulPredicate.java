package nl.wernerdegroot.guetta.core.optics.supporting;

/**
 * Represents a predicate that takes a value and an initial state and returns a boolean and a new state.
 *
 * @param <Value> The type of the value to test.
 * @param <State> The type of the state.
 */
@FunctionalInterface
public interface StatefulPredicate<Value, State> {

    StatePair<Boolean, State> test(Value value, State initialState);
}
