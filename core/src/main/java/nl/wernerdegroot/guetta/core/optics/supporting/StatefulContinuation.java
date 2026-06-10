package nl.wernerdegroot.guetta.core.optics.supporting;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface StatefulContinuation<Result, State, Value> {

    Stateful<Result, State> run(State initialState, BiFunction<? super Value, ? super State, ? extends Stateful<Result, State>> runner);

    default <T> StatefulContinuation<Result, State, T> map(Function<? super Value, ? extends T> fn) {
        return (initialState, runner) -> this.run(initialState, (value, state) -> runner.apply(fn.apply(value), state));
    }

    default <T> StatefulContinuation<Result, State, T> flatMap(Function<? super Value, ? extends StatefulContinuation<Result, State, T>> fn) {
        return (initialState, runner) -> this.run(initialState, (value, state) -> fn.apply(value).run(state, runner));
    }
}
