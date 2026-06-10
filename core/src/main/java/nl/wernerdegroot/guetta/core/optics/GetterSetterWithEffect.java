package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.GetterSetterWithEffectImpl;

import java.util.function.Predicate;

public interface GetterSetterWithEffect<Structure, Value> extends GetterSetter<Structure, Value>, SetterWithEffect<Structure, Value>, EachGetterSetterWithEffect<Structure, Value> {

    static <Structure, Value> GetterSetterWithEffect<Structure, Value> from(Getter<Structure, Value> getter, SetterWithEffect<Structure, Value> setterWithEffect) {
        return new GetterSetterWithEffectImpl<>(getter, setterWithEffect);
    }

    default <T> GetterSetterWithEffect<Structure, T> andThen(GetterSetterWithEffect<Value, T> that) {
        var getter = this.asGetter().andThen(that);
        var setterWithEffect = this.asSetterWithEffect().andThen(that);
        return new GetterSetterWithEffectImpl<>(getter, setterWithEffect);
    }

    @Override
    default GetterSetterWithEffect<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }
}
