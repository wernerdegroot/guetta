package nl.wernerdegroot.guetta.core.optics.supporting;

import java.util.function.Function;

/**
 * Represents a computation that takes an initial state and returns a value and a new state.
 *
 * @param <State> The type of the state.
 * @param <Value> The type of the value produced.
 */
@FunctionalInterface
public interface StatefulOperation<Value, State> {

   StatePair<Value, State> run(State initialState);

    static <Value, State> StatefulOperation<Value, State> pure(Value value) {
        return state -> StatePair.of(value, state);
    }

    default <T> StatefulOperation<T, State> map(Function<? super Value, ? extends T> fn) {
        return state -> this.run(state).map(fn);
    }

    default <T> StatefulOperation<T, State> flatMap(Function<? super Value, ? extends StatefulOperation<T, State>> fn) {
        return state -> {
            var result = this.run(state);
            var that = fn.apply(result.value());
            return that.run(result.state());
        };
    }
}
