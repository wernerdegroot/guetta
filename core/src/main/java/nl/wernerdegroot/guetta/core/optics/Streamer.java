package nl.wernerdegroot.guetta.core.optics;

import java.util.Collection;
import java.util.function.Predicate;
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

    default Streamer<Structure, Value> filter(Predicate<Value> predicate) {
        return structure -> stream(structure).filter(predicate);
    }

    default Streamer<Structure, Value> take(int n) {
        return structure -> stream(structure).limit(n);
    }

    default Streamer<Structure, Value> takeWhile(Predicate<Value> predicate) {
        return structure -> stream(structure).takeWhile(predicate);
    }
}
