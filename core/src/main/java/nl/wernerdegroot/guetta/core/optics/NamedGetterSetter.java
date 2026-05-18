package nl.wernerdegroot.guetta.core.optics;

/**
 * A {@link GetterSetter} that also has a name, typically representing the component name.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value
 */
public interface NamedGetterSetter<Structure, Value> extends GetterSetter<Structure, Value> {

    /**
     * Returns the name of the component this getter-setter operates on.
     *
     * @return the name
     */
    String getName();
}
