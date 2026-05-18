package nl.wernerdegroot.guetta.core.optics;

/**
 * A functional interface for getting a value from a structure.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value
 */
@FunctionalInterface
public interface Getter<Structure, Value> {

    /**
     * Gets the value from the structure.
     *
     * @param structure the structure to get the value from
     * @return the value
     */
    Value get(Structure structure);
}
