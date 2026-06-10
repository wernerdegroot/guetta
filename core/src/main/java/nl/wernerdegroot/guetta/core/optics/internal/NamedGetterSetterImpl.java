package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.Getter;
import nl.wernerdegroot.guetta.core.optics.GetterSetter;
import nl.wernerdegroot.guetta.core.optics.NamedGetterSetter;
import nl.wernerdegroot.guetta.core.optics.Setter;

import java.util.function.UnaryOperator;

public record NamedGetterSetterImpl<Structure, Value>(
        String name,
        Getter<Structure, Value> getter,
        Setter<Structure, Value> setter
) implements NamedGetterSetter<Structure, Value> {

    @Override
    public Value get(Structure structure) {
        return getter.get(structure);
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        return setter.modify(structure, modifier);
    }
}
