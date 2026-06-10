package nl.wernerdegroot.guetta.core;

import nl.wernerdegroot.guetta.core.optics.*;
import nl.wernerdegroot.guetta.core.optics.internal.NamedMethod;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class Focus {

    private Focus() {

    }

    /**
     * Creates a {@link GetterSetterWithEffect} from a method reference to a record component.
     * <p>
     * The method reference must be to a component of a Java record. This method uses reflection
     * to extract the component name and provide both a getter and a setter (which creates a new
     * record instance with the updated value).
     */
    public static <Structure, Value> NamedGetterSetterWithEffect<Structure, Value> on(SerializableFunction<Structure, Value> methodReference) {
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
                var accessor = component.getAccessor();
                accessor.setAccessible(true);

                @SuppressWarnings("unchecked")
                var value = (Value) accessor.invoke(record);

                return value;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not invoke getter " + methodName + " on " + record.getClass().getName(), e);
            }
        };

        SetterWithEffect<Structure, Value> setterWithEffect = new SetterWithEffect<>() {
            @Override
            public <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect) {
                Objects.requireNonNull(structure, "Record must not be null");

                if (!structure.getClass().equals(clazz)) {
                    throw new IllegalArgumentException("Expected record of type " + clazz.getName() + " but got " + structure.getClass().getName());
                }

                try {
                    var recordComponents = clazz.getRecordComponents();
                    var values = new Object[recordComponents.length];
                    var parameterTypes = new Class<?>[recordComponents.length];

                    var recordComponentIndex = -1;

                    for (var i = 0; i < recordComponents.length; i++) {
                        var recordComponent = recordComponents[i];
                        var recordComponentAccessor = recordComponent.getAccessor();
                        recordComponentAccessor.setAccessible(true);

                        values[i] = recordComponentAccessor.invoke(structure);
                        parameterTypes[i] = recordComponent.getType();

                        if (recordComponent.getName().equals(methodName)) {
                            recordComponentIndex = i;
                        }
                    }

                    if (recordComponentIndex == -1) {
                        throw new RuntimeException("Method " + methodName + " is not a record component of " + clazz.getName());
                    }

                    var constructor = clazz.getDeclaredConstructor(parameterTypes);
                    constructor.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    var valueToModify = (Value) values[recordComponentIndex];

                    var effectivelyFinalRecordComponentIndex = recordComponentIndex;
                    return modifierWithEffect.apply(valueToModify).map(modifiedValue -> {
                        var arguments = Arrays.copyOf(values, values.length);
                        arguments[effectivelyFinalRecordComponentIndex] = modifiedValue;

                        try {
                            @SuppressWarnings("unchecked")
                            var updated = (Structure) constructor.newInstance(arguments);

                            return updated;
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException("Could not copy record " + clazz.getName(), e);
                        }
                    });
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Could not copy record " + clazz.getName(), e);
                }
            }
        };

        return NamedGetterSetterWithEffect.from(methodName, getter, setterWithEffect);
    }

    public static <Structure, Value> EachGetterSetterWithEffect<Structure, Value> each(SerializableFunction<Structure, ? extends Collection<Value>> methodReference) {
        var namedMethod = NamedMethod.from(methodReference);
        var methodName = namedMethod.getMethodName();
        var clazz = namedMethod.getTargetClass();

        if (!clazz.isRecord()) {
            throw new RuntimeException("Class " + clazz.getName() + " is not a record");
        }

        var component = Arrays.stream(clazz.getRecordComponents()).filter(recordComponent -> recordComponent.getName().equals(methodName)).findFirst().orElseThrow(() -> new RuntimeException("Method " + methodName + " is not a record component of " + clazz.getName()));

        var componentType = component.getType();

        if (!Collection.class.isAssignableFrom(componentType)) {
            throw new RuntimeException("Method " + methodName + " must return a collection");
        }

        return Focus.on(methodReference).andThen(EachGetterSetterWithEffect.from(componentType));
    }
}
