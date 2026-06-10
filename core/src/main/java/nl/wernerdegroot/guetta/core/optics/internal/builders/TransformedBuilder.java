package nl.wernerdegroot.guetta.core.optics.internal.builders;

import java.util.function.Function;

public class TransformedBuilder<Structure, TransformedStructure, Accumulator, Value> implements Builder<TransformedStructure, Accumulator, Value> {

    private final Builder<Structure, Accumulator, Value> builder;
    private final Function<? super Structure, ? extends TransformedStructure> transformer;

    public TransformedBuilder(Builder<Structure, Accumulator, Value> builder, Function<? super Structure, ? extends TransformedStructure> transformer) {
        this.builder = builder;
        this.transformer = transformer;
    }

    @Override
    public Accumulator getAccumulator() {
        return builder.getAccumulator();
    }

    @Override
    public Accumulator addValue(Accumulator accumulator, Value value) {
        return builder.addValue(accumulator, value);
    }

    @Override
    public TransformedStructure build(Accumulator accumulator) {
        return transformer.apply(builder.build(accumulator));
    }
}
