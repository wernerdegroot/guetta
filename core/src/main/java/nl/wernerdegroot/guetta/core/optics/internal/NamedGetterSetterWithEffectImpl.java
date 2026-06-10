package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.Getter;
import nl.wernerdegroot.guetta.core.optics.NamedGetterSetterWithEffect;
import nl.wernerdegroot.guetta.core.optics.SetterWithEffect;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public record NamedGetterSetterWithEffectImpl<Structure, Value>(
        String name,
        Getter<Structure, Value> getter,
        SetterWithEffect<Structure, Value> setterWithEffect
) implements NamedGetterSetterWithEffect<Structure, Value> {

    @Override
    public Value get(Structure structure) {
        return getter.get(structure);
    }

    @Override
    public Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        return modifyWithIdentity(structure, modifier);
    }

    @Override
    public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect) {
        return setterWithEffect.modifyWithEffect(structure, modifierWithEffect);
    }
}
