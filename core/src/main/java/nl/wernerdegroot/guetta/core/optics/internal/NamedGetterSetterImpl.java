package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.Getter;
import nl.wernerdegroot.guetta.core.optics.GetterSetter;
import nl.wernerdegroot.guetta.core.optics.NamedGetterSetter;
import nl.wernerdegroot.guetta.core.optics.Setter;

public record NamedGetterSetterImpl<Structure, Value>(GetterSetter<Structure, Value> getterSetter,
                                                      String name) implements NamedGetterSetter<Structure, Value> {

    public NamedGetterSetterImpl(Getter<Structure, Value> getter, Setter<Structure, Value> setter, String name) {
        this(new GetterSetterImpl<>(getter, setter), name);
    }

    @Override
    public Value get(Structure structure) {
        return getterSetter.get(structure);
    }

    @Override
    public Structure set(Structure structure, Value value) {
        return getterSetter.set(structure, value);
    }

    @Override
    public String getName() {
        return name;
    }
}
