package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.GetterSetterImpl;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A lens that can both get and set a value within a structure.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value within the structure
 */
public interface GetterSetter<Structure, Value> extends Getter<Structure, Value>, Setter<Structure, Value>, EachGetterSetter<Structure, Value> {

    static <Structure, Value> GetterSetter<Structure, Value> from(Getter<Structure, Value> getter, Setter<Structure, Value> setter) {
        return new GetterSetterImpl<>(getter, setter);
    }

    /**
     * Composes this {@link GetterSetter} with another {@link GetterSetter}.
     *
     * @param that the getter-setter to compose with
     * @param <T>  the type of the value in the resulting getter-setter
     * @return a new getter-setter
     */
    default <T> GetterSetter<Structure, T> andThen(GetterSetter<Value, T> that) {
        var getter = this.asGetter().andThen(that);
        var setter = this.asSetter().andThen(that);
        return new GetterSetterImpl<>(getter, setter);
    }

    @Override
    default GetterSetter<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }
}
