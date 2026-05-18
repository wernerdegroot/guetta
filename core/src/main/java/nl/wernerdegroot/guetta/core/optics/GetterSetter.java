package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.GetterSetterImpl;
import nl.wernerdegroot.guetta.core.optics.internal.NamedGetterSetterImpl;
import nl.wernerdegroot.guetta.core.optics.internal.NamedMethod;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * A lens that can both get and set a value within a structure.
 *
 * @param <Structure> the type of the structure
 * @param <Value>     the type of the value within the structure
 */
public interface GetterSetter<Structure, Value> {

    /**
     * Creates a {@link NamedGetterSetter} from a method reference to a record component.
     * <p>
     * The method reference must be to a component of a Java record. This method uses reflection
     * to extract the component name and provide both a getter and a setter (which creates a new
     * record instance with the updated value).
     *
     * @param methodReference a method reference to a record component (e.g., {@code Person::name})
     * @param <Structure>     the type of the record
     * @param <Value>         the type of the component
     * @return a {@link NamedGetterSetter} for the specified component
     * @throws NullPointerException if {@code methodReference} is {@code null}
     * @throws RuntimeException     if the method reference is invalid or the class is not a record
     */
    static <Structure, Value> NamedGetterSetter<Structure, Value> from(SerializableFunction<Structure, Value> methodReference) {
        Objects.requireNonNull(methodReference, "Method reference must not be null");

        var namedMethod = NamedMethod.from(methodReference);
        var methodName = namedMethod.getMethodName();
        var clazz = namedMethod.getTargetClass();

        if (!clazz.isRecord()) {
            throw new RuntimeException("Class " + clazz.getName() + " is not a record");
        }

        var component = Arrays.stream(clazz.getRecordComponents())
                .filter(recordComponent -> recordComponent.getName().equals(methodName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Method " + methodName + " is not a record component of " + clazz.getName()));

        Getter<Structure, Value> getter = record -> {
            Objects.requireNonNull(record, "Record must not be null");

            if (!record.getClass().equals(clazz)) {
                throw new IllegalArgumentException("Expected record of type " + clazz.getName() + " but got " + record.getClass().getName());
            }

            try {
                @SuppressWarnings("unchecked")
                var value = (Value) component.getAccessor().invoke(record);

                return value;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not invoke getter " + methodName + " on " + record.getClass().getName(), e);
            }
        };

        Setter<Structure, Value> setter = (record, value) -> {
            Objects.requireNonNull(record, "Record must not be null");

            if (!record.getClass().equals(clazz)) {
                throw new IllegalArgumentException("Expected record of type " + clazz.getName() + " but got " + record.getClass().getName());
            }

            try {
                var recordComponents = clazz.getRecordComponents();
                var arguments = new Object[recordComponents.length];
                var parameterTypes = new Class<?>[recordComponents.length];

                for (int i = 0; i < recordComponents.length; i++) {
                    var recordComponent = recordComponents[i];
                    parameterTypes[i] = recordComponent.getType();
                    arguments[i] = recordComponent.getName().equals(methodName)
                            ? value
                            : recordComponent.getAccessor().invoke(record);
                }

                var constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);

                @SuppressWarnings("unchecked")
                var updated = (Structure) constructor.newInstance(arguments);

                return updated;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not copy record " + clazz.getName(), e);
            }
        };

        return new NamedGetterSetterImpl<>(getter, setter, methodName);
    }

    /**
     * Gets the value from the structure.
     *
     * @param structure the structure to get the value from
     * @return the value
     */
    Value get(Structure structure);

    /**
     * Sets the value in the structure, returning a new structure.
     *
     * @param structure the structure to set the value in
     * @param value     the new value
     * @return a new structure with the updated value
     */
    Structure set(Structure structure, Value value);

    /**
     * Modifies the value in the structure using the provided modifier function, returning a new structure.
     *
     * @param structure the structure to modify
     * @param modifier  the function to apply to the value
     * @return a new structure with the modified value
     */
    default Structure modify(Structure structure, UnaryOperator<Value> modifier) {
        var value = get(structure);
        var modified = modifier.apply(value);
        return set(structure, modified);
    }

    /**
     * Composes this {@link GetterSetter} with a {@link Getter}, resulting in a new {@link Getter}.
     *
     * @param that the getter to compose with
     * @param <T>  the type of the value in the resulting getter
     * @return a new getter
     */
    default <T> Getter<Structure, T> andThenGet(Getter<Value, T> that) {
        return structure -> that.get(this.get(structure));
    }

    /**
     * Composes this {@link GetterSetter} with a {@link Setter}, resulting in a new {@link Setter}.
     *
     * @param that the setter to compose with
     * @param <T>  the type of the value in the resulting setter
     * @return a new setter
     */
    default <T> Setter<Structure, T> andThenSet(Setter<Value, T> that) {
        return (structure, value) -> {
            var intermediate = this.get(structure);
            var modifiedIntermediate = that.set(intermediate, value);
            return this.set(structure, modifiedIntermediate);
        };
    }

    /**
     * Composes this {@link GetterSetter} with another {@link GetterSetter}.
     *
     * @param that the getter-setter to compose with
     * @param <T>  the type of the value in the resulting getter-setter
     * @return a new getter-setter
     */
    default <T> GetterSetter<Structure, T> andThen(GetterSetter<Value, T> that) {
        var getter = this.andThenGet(that::get);
        var setter = this.andThenSet(that::set);
        return new GetterSetterImpl<>(getter, setter);
    }

    /**
     * Composes this {@link GetterSetter} with a method reference to a record component.
     *
     * @param serializableFunction a method reference to a record component
     * @param <T>                  the type of the value in the resulting getter-setter
     * @return a new getter-setter
     */
    default <T> GetterSetter<Structure, T> andThen(SerializableFunction<Value, T> serializableFunction) {
        return this.andThen(GetterSetter.from(serializableFunction));
    }
}
