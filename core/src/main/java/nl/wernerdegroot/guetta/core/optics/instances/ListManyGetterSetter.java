package nl.wernerdegroot.guetta.core.optics.instances;

import nl.wernerdegroot.guetta.core.optics.ManyGetterSetter;
import nl.wernerdegroot.guetta.core.optics.ModifiableWithEffect;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ListManyGetterSetter<T> implements ManyGetterSetter<List<T>, T>, ModifiableWithEffect<List<T>, T> {

    @Override
    public Stream<T> stream(List<T> structure) {
        return structure.stream();
    }

    @Override
    public List<T> modify(List<T> structure, UnaryOperator<T> modifier) {
        return structure.stream().map(modifier).toList();
    }

    @Override
    public <Result> Continuation<Result, List<T>> modifyWithEffect(List<T> structure, Function<T, Continuation<Result, T>> modifierWithEffect) {
        Continuation<Result, List<T>> resultContinuation = Continuation.of(new ArrayList<>());

        for (var value : structure) {
            resultContinuation = resultContinuation.flatMap(result ->
                    modifierWithEffect.apply(value).map(modifiedValue -> {
                        List<T> copy = new ArrayList<>(result);
                        copy.add(modifiedValue);
                        return copy;
                    })
            );
        }

        return resultContinuation;
    }
}
