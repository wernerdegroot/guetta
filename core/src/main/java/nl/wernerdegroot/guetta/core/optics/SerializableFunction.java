package nl.wernerdegroot.guetta.core.optics;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A {@link java.util.function.Function} that is also {@link java.io.Serializable}.
 * <p>
 * This is required for extracting method names from method references using reflection.
 *
 * @param <P> the type of the parameter
 * @param <R> the type of the result
 */
@FunctionalInterface
public interface SerializableFunction<P, R> extends Function<P, R>, Serializable {

    R apply(P parameter);
}
