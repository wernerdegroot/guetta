package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.ManyGetterSetter;
import nl.wernerdegroot.guetta.core.optics.Modifiable;
import nl.wernerdegroot.guetta.core.optics.Streamer;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record ManyGetterSetterImpl<Structure, Value>(Streamer<Structure, Value> streamer, Modifiable<Structure, Value> modifiable) implements ManyGetterSetter<Structure, Value> {

    @Override
    public Stream<Value> stream(Structure structure) {
        return streamer.stream(structure);
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        return modifiable.modify(structure, modifier);
    }
}
