package nl.wernerdegroot.guetta.core.optics;

import java.util.Collection;
import java.util.stream.Stream;

public interface Streamer<Structure, Value> {

    static <Structure, Value> Streamer<Structure, Value> from(Getter<? super Structure, ? extends Collection<Value>> getter) {
        return structure -> getter.get(structure).stream();
    }

    static Streamer<String, Character> characters() {
        return structure -> structure.chars().mapToObj(i -> (char) i);
    }

    Stream<Value> stream(Structure structure);

    default Streamer<Structure, Value> asStreamer() {
        return this;
    }

    default <T> Streamer<Structure, T> andThen(Streamer<Value, T> that) {
        return structure -> this.stream(structure).flatMap(that::stream);
    }
}
