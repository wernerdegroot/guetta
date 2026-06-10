package nl.wernerdegroot.guetta.core.optics.internal.builders;

import java.util.function.Function;

public interface Builder<Structure, Accumulator, Value> {

    Accumulator getAccumulator();

    Accumulator addValue(Accumulator accumulator, Value value);

    Structure build(Accumulator accumulator);

    default <TransformedStructure> Builder<TransformedStructure, Accumulator, Value> as(Function<? super Structure, ? extends TransformedStructure> transformer) {
        return new TransformedBuilder<>(this, transformer);
    }
}
