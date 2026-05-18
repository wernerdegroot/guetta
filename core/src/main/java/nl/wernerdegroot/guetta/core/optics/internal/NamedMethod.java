package nl.wernerdegroot.guetta.core.optics.internal;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class NamedMethod {
    private final Class<?> targetClass;
    private final String methodName;

    private NamedMethod(final Class<?> targetClass, final String methodName) {
        this.targetClass = targetClass;
        this.methodName = methodName;
    }

    public static NamedMethod from(final Serializable methodReference) {
        Objects.requireNonNull(methodReference, "methodReference must not be null");
        try {
            var methodReferenceClass = methodReference.getClass();
            var writeReplaceMethod = methodReferenceClass.getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            var serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(methodReference);
            var className = serializedLambda.getImplClass().replace('/', '.');
            var classLoader = Objects.requireNonNullElse(methodReferenceClass.getClassLoader(), NamedMethod.class.getClassLoader());

            var clazz = Class.forName(className, true, classLoader);
            var methodName = serializedLambda.getImplMethodName();
            
            if (serializedLambda.getImplMethodKind() != java.lang.invoke.MethodHandleInfo.REF_invokeVirtual) {
                throw new RuntimeException("Method reference must be a virtual method call (e.g., Person::name)");
            }

            return new NamedMethod(clazz, methodName);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | ClassNotFoundException e) {
            throw new RuntimeException("Invalid method reference", e);
        }
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }
}
