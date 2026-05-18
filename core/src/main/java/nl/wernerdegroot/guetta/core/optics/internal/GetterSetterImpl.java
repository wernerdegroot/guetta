package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.Getter;
import nl.wernerdegroot.guetta.core.optics.GetterSetter;
import nl.wernerdegroot.guetta.core.optics.Setter;

import java.util.Objects;

public record GetterSetterImpl<Structure, Value>(
        Getter<Structure, Value> getter,
        Setter<Structure, Value> setter
) implements GetterSetter<Structure, Value> {

    public GetterSetterImpl {
        Objects.requireNonNull(getter, "getter must not be null");
        Objects.requireNonNull(setter, "setter must not be null");
    }

    @Override
    public Value get(Structure structure) {
        return getter.get(structure);
    }

    @Override
    public Structure set(Structure structure, Value value) {
        return setter.set(structure, value);
    }
}
