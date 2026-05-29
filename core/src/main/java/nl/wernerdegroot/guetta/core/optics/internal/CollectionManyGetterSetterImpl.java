package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.ManyGetterSetter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CollectionManyGetterSetterImpl<T, C extends Collection<T>> implements ManyGetterSetter<C, T> {

    private static final Map<Class<?>, CollectionManyGetterSetterImpl<?, ?>> INSTANCES_BY_CLASS = new HashMap<>();

    static {
        INSTANCES_BY_CLASS.put(Collection.class, new CollectionManyGetterSetterImpl<Object, Collection<Object>>(Collectors.toCollection(ArrayList::new)));
    }

    private final Collector<T, ?, C> collector;

    public CollectionManyGetterSetterImpl(Collector<T, ?, C> collector) {
        this.collector = collector;
    }

    public static <T, C extends Collection<T>> ManyGetterSetter<C, T> getInstance(C structure, Class<?> declaredType) {
        if (structure == null) {
            return getInstance(declaredType);
        }

        @SuppressWarnings("unchecked")
        var result = (ManyGetterSetter<C, T>) getInstance(structure.getClass());

        return result;
    }

    public static <T, C extends Collection<T>> ManyGetterSetter<C, T> getInstance(Class<?> declaredType) {
        Class<?> key = declaredType;

        @SuppressWarnings("unchecked")
        var value = (ManyGetterSetter<C, T>) INSTANCES_BY_CLASS.get(key);

        if (value == null) {
            value = getInstance(key.getSuperclass());
        }

        return null;
    }

    @Override
    public C modify(C structure, UnaryOperator<T> modifier) {
        return structure.stream().map(modifier).collect(collector);
    }

    @Override
    public Stream<T> stream(C structure) {
        return structure.stream();
    }
}
