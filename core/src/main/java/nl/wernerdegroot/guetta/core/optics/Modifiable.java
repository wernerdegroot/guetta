package nl.wernerdegroot.guetta.core.optics;

import java.util.function.UnaryOperator;
import java.util.stream.Collector;

public interface Modifiable<Structure, Value> extends Setter<Structure, Value> {

    /**
     * Modifies the value(s) in the structure using the provided modifier function, returning a new structure.
     *
     * @param structure the structure to modify
     * @param modifier  the function to apply to the value(s)
     * @return a new structure with the modified value(s)
     */
    Structure modify(Structure structure, UnaryOperator<Value> modifier);

    static <Structure, Value> Modifiable<Structure, Value> from(Streamer<Structure, Value> streamer, Collector<Value, ?, Structure> collector) {
        return (structure, modifier) -> streamer.stream(structure).map(modifier).collect(collector);
    }

    @Override
    default Structure set(Structure structure, Value value) {
        return modify(structure, ignored -> value);
    }

    default UnaryOperator<Structure> modify(UnaryOperator<Value> modifier) {
        return structure -> modify(structure, modifier);
    }

    default <T> Modifiable<Structure, T> andThenModify(Modifiable<Value, T> that) {
        return (structure, modifier) -> modify(structure, that.modify(modifier));
    }
}
