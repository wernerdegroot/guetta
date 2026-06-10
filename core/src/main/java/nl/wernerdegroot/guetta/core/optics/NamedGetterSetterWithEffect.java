package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.NamedGetterSetterWithEffectImpl;

import java.util.function.Predicate;

public interface NamedGetterSetterWithEffect<Structure, Value> extends Named, GetterSetterWithEffect<Structure, Value> {

    static <Structure, Value> NamedGetterSetterWithEffect<Structure, Value> from(String name, Getter<Structure, Value> getter, SetterWithEffect<Structure, Value> setterWithEffect) {
        return new NamedGetterSetterWithEffectImpl<>(name, getter, setterWithEffect);
    }

    @Override
    default NamedGetterSetterWithEffect<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }
}
