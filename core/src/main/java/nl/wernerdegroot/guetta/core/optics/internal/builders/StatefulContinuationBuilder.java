package nl.wernerdegroot.guetta.core.optics.internal.builders;

import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import nl.wernerdegroot.guetta.core.optics.supporting.Stateful;

import java.util.function.BiFunction;

public class StatefulContinuationBuilder<Result, Structure, Accumulator, Value, State> implements Builder<Continuation<Result, Stateful<Structure, State>>, Continuation<Result, Stateful<Accumulator, State>>, Continuation<Result, Stateful<Value, State>>> {

    private final Builder<Structure, Accumulator, Value> builder;

    public StatefulContinuationBuilder(Builder<Structure, Accumulator, Value> builder) {
        this.builder = builder;
    }

    public Continuation<Result, Stateful<Accumulator, State>> getAccumulator(State initialState) {
        return Continuation.of(Stateful.of(builder.getAccumulator(), initialState));
    }

    @Override
    public Continuation<Result, Stateful<Accumulator, State>> getAccumulator() {
        throw new UnsupportedOperationException("Use getAccumulator(State initialState) instead");
    }

    @Override
    public Continuation<Result, Stateful<Accumulator, State>> addValue(Continuation<Result, Stateful<Accumulator, State>> accumulatorContinuation, Continuation<Result, Stateful<Value, State>> valueContinuation) {
        throw new UnsupportedOperationException("Use addValue(Continuation, BiFunction, Value) instead");
    }

    public Continuation<Result, Stateful<Accumulator, State>> addValue(Continuation<Result, Stateful<Accumulator, State>> accumulatorContinuation, BiFunction<Value, State, Continuation<Result, Stateful<Value, State>>> modifier, Value originalValue) {
        return accumulatorContinuation.flatMap(acc -> {
            return modifier.apply(originalValue, acc.state()).map(val -> {
                Accumulator nextAccumulator = builder.addValue(acc.value(), val.value());
                return new Stateful<>(nextAccumulator, val.state());
            });
        });
    }

    @Override
    public Continuation<Result, Stateful<Structure, State>> build(Continuation<Result, Stateful<Accumulator, State>> accumulatorContinuation) {
        return accumulatorContinuation.map(acc -> acc.mapValue(builder::build));
    }
}
