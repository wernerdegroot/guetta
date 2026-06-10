package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.CollectionEachGetterSetterWithEffect;
import nl.wernerdegroot.guetta.core.optics.internal.builders.Builder;
import nl.wernerdegroot.guetta.core.optics.internal.builders.BuilderRegistry;
import nl.wernerdegroot.guetta.core.optics.internal.EachGetterSetterWithEffectImpl;

import java.util.Collection;
import java.util.function.Predicate;

public interface EachGetterSetterWithEffect<Structure, Value> extends EachGetterSetter<Structure, Value>, SetterWithEffect<Structure, Value> {

    static <Structure extends Collection<Value>, Value> EachGetterSetterWithEffect<Structure, Value> from(Class<?> clazz) {
        Builder<Structure, ?, Value> builder = BuilderRegistry.getBuilderFor(clazz);
        return new CollectionEachGetterSetterWithEffect<>(builder);
    }

    default <T> EachGetterSetterWithEffect<Structure, T> andThen(EachGetterSetterWithEffect<Value, T> that) {
        var streamer = this.asStreamer().andThen(that);
        var setter = this.asSetter().andThen(that);
        var setterWithEffect = this.asSetterWithEffect().andThen(that);
        return new EachGetterSetterWithEffectImpl<>(streamer, setter, setterWithEffect);
    }

    @Override
    default EachGetterSetterWithEffect<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    default EachGetterSetterWithEffect<Structure, Value> take(int n) {
        return new EachGetterSetterWithEffectImpl<>(
                structure -> asStreamer().stream(structure).limit(n),
                (structure, modifier) -> {
                    var counter = new Object() {
                        int count = 0;
                    };
                    return asSetter().modify(structure, value -> {
                        if (counter.count < n) {
                            counter.count++;
                            return modifier.apply(value);
                        } else {
                            return value;
                        }
                    });
                },
                SetterWithEffect.super.take(n)
        );
    }

    @Override
    default EachGetterSetterWithEffect<Structure, Value> takeWhile(java.util.function.Predicate<Value> predicate) {
        return new EachGetterSetterWithEffectImpl<>(
                structure -> asStreamer().stream(structure).takeWhile(predicate),
                (structure, modifier) -> {
                    var state = new Object() {
                        boolean taking = true;
                    };
                    return asSetter().modify(structure, value -> {
                        if (state.taking && predicate.test(value)) {
                            return modifier.apply(value);
                        } else {
                            state.taking = false;
                            return value;
                        }
                    });
                },
                SetterWithEffect.super.takeWhile(predicate)
        );
    }
}
