package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.*;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record EachGetterSetterWithEffectImpl<Structure, Value>(
        Streamer<Structure, Value> streamer,

        // Although you could remove `Setter` and use `SetterWithEffect` here,
        // this will be very efficient for large collections.
        Setter<Structure, Value> setter,

        SetterWithEffect<Structure, Value> setterWithEffect) implements EachGetterSetterWithEffect<Structure, Value> {

    @Override
    public Streamer<Structure, Value> asStreamer() {
        return streamer;
    }

    @Override
    public Setter<Structure, Value> asSetter() {
        return setter;
    }

    @Override
    public SetterWithEffect<Structure, Value> asSetterWithEffect() {
        return setterWithEffect;
    }

    @Override
    public Stream<Value> stream(Structure structure) {
        return streamer.stream(structure);
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        return setter.modify(structure, modifier);
    }

    @Override
    public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect) {
        return setterWithEffect.modifyWithEffect(structure, modifierWithEffect);
    }
}
