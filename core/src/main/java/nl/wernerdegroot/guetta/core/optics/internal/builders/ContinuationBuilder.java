package nl.wernerdegroot.guetta.core.optics.internal.builders;

import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

public class ContinuationBuilder<Result, Structure, Accumulator, Value> implements Builder<Continuation<Result, Structure>, Continuation<Result, Accumulator>, Continuation<Result, Value>> {

    private final Builder<Structure, Accumulator, Value> builder;

    public ContinuationBuilder(Builder<Structure, Accumulator, Value> builder) {
        this.builder = builder;
    }

    @Override
    public Continuation<Result, Accumulator> getAccumulator() {
        return Continuation.of(builder.getAccumulator());
    }

    @Override
    public Continuation<Result, Accumulator> addValue(Continuation<Result, Accumulator> accumulatorContinuation, Continuation<Result, Value> valueContinuation) {
        return accumulatorContinuation.flatMap(accumulator -> valueContinuation.map(value -> builder.addValue(accumulator, value)));
    }

    @Override
    public Continuation<Result, Structure> build(Continuation<Result, Accumulator> accumulatorContinuation) {
        return accumulatorContinuation.map(builder::build);
    }
}
