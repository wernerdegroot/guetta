package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.GetterSetterImpl;

public final class Guetta {

    private Guetta() {}

    public static <Structure, Value> GetterSetter<Structure, Value> on(SerializableFunction<Structure, Value> methodReference) {
        return GetterSetter.from(methodReference);
    }
}
