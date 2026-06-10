package nl.wernerdegroot.guetta.core.optics;

import java.util.stream.Stream;

/**
 * A functional interface for getting a value from a structure.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value
 */
@FunctionalInterface
public interface Getter<Structure, Value> extends Streamer<Structure, Value> {

    default Getter<Structure, Value> asGetter() {
        return this;
    }

    /**
     * Gets the value from the structure.
     *
     * @param structure the structure to get the value from
     * @return the value
     */
    Value get(Structure structure);

    @Override
    default Stream<Value> stream(Structure structure) {
        return Stream.of(get(structure));
    }

    default <T> Getter<Structure, T> andThen(Getter<Value, T> that) {
        return structure -> that.get(this.get(structure));
    }
}
