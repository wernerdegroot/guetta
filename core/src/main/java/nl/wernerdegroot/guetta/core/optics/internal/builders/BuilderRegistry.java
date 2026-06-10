package nl.wernerdegroot.guetta.core.optics.internal.builders;

import java.util.*;

public class BuilderRegistry {

    private static final Map<Class<?>, Builder<?, ?, ?>> BUILDERS_BY_CLASS;

    static {
        BUILDERS_BY_CLASS = new HashMap<>();

        BUILDERS_BY_CLASS.put(Collection.class, new ArrayListBuilder<>());
        BUILDERS_BY_CLASS.put(List.class, new ArrayListBuilder<>());
        BUILDERS_BY_CLASS.put(ArrayList.class, new ArrayListBuilder<>());
    }

    public static <Structure extends Collection<Value>, Value> Builder<Structure, ?, Value> getBuilderFor(Class<?> clazz) {
        @SuppressWarnings("unchecked")
        var builder = (Builder<Structure, ?, Value>) BUILDERS_BY_CLASS.get(clazz);

        if (builder == null) {
            throw new RuntimeException("No builder found for class " + clazz.getName());
        }

        return builder;
    }
}
