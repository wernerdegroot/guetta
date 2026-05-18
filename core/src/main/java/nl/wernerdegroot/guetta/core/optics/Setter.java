package nl.wernerdegroot.guetta.core.optics;

/**
 * A functional interface for setting a value in a structure, returning a new structure.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value
 */
@FunctionalInterface
public interface Setter<Structure, Value> {

    /**
     * Sets the value in the structure, returning a new structure.
     *
     * @param structure the structure to set the value in
     * @param value     the new value
     * @return a new structure with the updated value
     */
    Structure set(Structure structure, Value value);
}
