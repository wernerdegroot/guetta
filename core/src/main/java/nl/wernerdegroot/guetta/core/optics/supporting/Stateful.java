package nl.wernerdegroot.guetta.core.optics.supporting;

public record Stateful<Value, State>(Value value, State state) {

    public static <Value, State> Stateful<Value, State> of(Value value, State state) {
        return new Stateful<>(value, state);
    }

    public <T> Stateful<T, State> mapValue(java.util.function.Function<Value, T> fn) {
        return new Stateful<>(fn.apply(value), state);
    }

    public <S> Stateful<Value, S> mapState(java.util.function.Function<State, S> fn) {
        return new Stateful<>(value, fn.apply(state));
    }
}
