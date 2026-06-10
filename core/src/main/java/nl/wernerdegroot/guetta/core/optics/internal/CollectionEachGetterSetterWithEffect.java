package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.EachGetterSetterWithEffect;
import nl.wernerdegroot.guetta.core.optics.internal.builders.Builder;
import nl.wernerdegroot.guetta.core.optics.internal.builders.ContinuationBuilder;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class CollectionEachGetterSetterWithEffect<Structure extends Collection<Value>, Accumulator, Value> implements EachGetterSetterWithEffect<Structure, Value> {

    private final Builder<Structure, Accumulator, Value> builder;

    public CollectionEachGetterSetterWithEffect(Builder<Structure, Accumulator, Value> builder) {
        this.builder = builder;
    }

    @Override
    public Stream<Value> stream(Structure structure) {
        return structure.stream();
    }

    @Override
    public CollectionEachGetterSetterWithEffect<Structure, Accumulator, Value> filter(java.util.function.Predicate<Value> predicate) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        var accumulator = builder.getAccumulator();

        for (var value : structure) {
            var modifiedValue = modifier.apply(value);
            accumulator = builder.addValue(accumulator, modifiedValue);
        }

        return builder.build(accumulator);
    }

    @Override
    public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect) {
        var continuationBuilder = new ContinuationBuilder<Result, Structure, Accumulator, Value>(builder);

        var accumulatorContinuation = continuationBuilder.getAccumulator();

        for (var value : structure) {
            var modifiedValueContinuation = modifierWithEffect.apply(value);
            accumulatorContinuation = continuationBuilder.addValue(accumulatorContinuation, modifiedValueContinuation);
        }

        return continuationBuilder.build(accumulatorContinuation);
    }
}
