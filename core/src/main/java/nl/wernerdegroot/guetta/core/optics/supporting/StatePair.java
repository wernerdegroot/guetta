package nl.wernerdegroot.guetta.core.optics.supporting;

import java.util.function.Function;

public record StatePair<Value, State>(Value value, State state) {

    public static <Value, State> StatePair<Value, State> of(Value value, State state) {
        return new StatePair<>(value, state);
    }

    public <T> StatePair<T, State> map(Function<? super Value, ? extends T> fn) {
        return StatePair.of(fn.apply(value), state);
    }
}
