package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.EachGetterSetter;
import nl.wernerdegroot.guetta.core.optics.Setter;
import nl.wernerdegroot.guetta.core.optics.Streamer;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record EachGetterSetterImpl<Structure, Value>(Streamer<Structure, Value> streamer, Setter<Structure, Value> setter) implements EachGetterSetter<Structure, Value> {

    @Override
    public Streamer<Structure, Value> asStreamer() {
        return streamer;
    }

    @Override
    public Setter<Structure, Value> asSetter() {
        return setter;
    }

    @Override
    public Stream<Value> stream(Structure structure) {
        return streamer.stream(structure);
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        return setter.modify(structure, modifier);
    }
}
