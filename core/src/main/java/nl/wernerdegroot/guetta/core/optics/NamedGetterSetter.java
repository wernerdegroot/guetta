package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.NamedGetterSetterImpl;

import java.util.function.Predicate;

public interface NamedGetterSetter<Structure, Value> extends Named, GetterSetter<Structure, Value> {

    static <Structure, Value> NamedGetterSetter<Structure, Value> from(String name, Getter<Structure, Value> getter, Setter<Structure, Value> setter) {
        return new NamedGetterSetterImpl<>(name, getter, setter);
    }

    @Override
    default NamedGetterSetter<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }
}
