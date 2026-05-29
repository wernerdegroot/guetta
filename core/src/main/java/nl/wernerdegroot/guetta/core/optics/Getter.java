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

    static <Structure, Value> Getter<Structure, Value> from(Getter<Structure, Value> getter) {
        return getter;
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

    default Getter<Structure, Value> asGetter() {
        return this;
    }

    /**
     * Composes this {@link Getter} with another {@link Getter}, resulting in a new {@link Getter}.
     *
     * @param that the getter to compose with
     * @param <T>  the type of the value in the resulting getter
     * @return a new getter
     */
    default <T> Getter<Structure, T> andThen(Getter<Value, T> that) {
        return structure -> that.get(this.get(structure));
    }
}
