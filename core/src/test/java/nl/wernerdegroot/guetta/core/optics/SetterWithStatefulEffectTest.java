package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import nl.wernerdegroot.guetta.core.optics.supporting.Stateful;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetterWithStatefulEffectTest {

    @Test
    void take_should_only_modify_first_n_elements() {
        SetterWithStatefulEffect<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        AtomicInteger callCount = new AtomicInteger(0);

        SetterWithStatefulEffect<List<Integer>, Integer> takeTwo = listSetter.take(2);

        Stateful<Continuation<List<Integer>, List<Integer>>, String> result = takeTwo.modifyWithEffect(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(Continuation.of(val * 10), state + ".");
        });

        List<Integer> finalValue = result.value().run(v -> v);
        assertEquals(List.of(10, 20, 3, 4, 5), finalValue);
        assertEquals(2, callCount.get());
        assertEquals("initial..", result.state());
    }

    @Test
    void drop_should_only_modify_elements_after_n() {
        SetterWithStatefulEffect<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        AtomicInteger callCount = new AtomicInteger(0);

        SetterWithStatefulEffect<List<Integer>, Integer> dropTwo = listSetter.drop(2);

        Stateful<Continuation<List<Integer>, List<Integer>>, String> result = dropTwo.modifyWithEffect(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(Continuation.of(val * 10), state + ".");
        });

        List<Integer> finalValue = result.value().run(v -> v);
        assertEquals(List.of(1, 2, 30, 40, 50), finalValue);
        assertEquals(3, callCount.get());
        assertEquals("initial...", result.state());
    }

    @Test
    void takeWhile_should_only_modify_while_predicate_holds() {
        SetterWithStatefulEffect<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 2, 1);
        AtomicInteger callCount = new AtomicInteger(0);

        SetterWithStatefulEffect<List<Integer>, Integer> takeWhileLessThree = listSetter.takeWhile(val -> val < 3);

        Stateful<Continuation<List<Integer>, List<Integer>>, String> result = takeWhileLessThree.modifyWithEffect(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(Continuation.of(val * 10), state + ".");
        });

        List<Integer> finalValue = result.value().run(v -> v);
        assertEquals(List.of(10, 20, 3, 2, 1), finalValue);
        assertEquals(2, callCount.get());
        assertEquals("initial..", result.state());
    }

    @Test
    void dropWhile_should_only_modify_after_predicate_fails() {
        SetterWithStatefulEffect<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 2, 1);
        AtomicInteger callCount = new AtomicInteger(0);

        SetterWithStatefulEffect<List<Integer>, Integer> dropWhileLessThree = listSetter.dropWhile(val -> val < 3);

        Stateful<Continuation<List<Integer>, List<Integer>>, String> result = dropWhileLessThree.modifyWithEffect(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(Continuation.of(val * 10), state + ".");
        });

        List<Integer> finalValue = result.value().run(v -> v);
        assertEquals(List.of(1, 2, 30, 20, 10), finalValue);
        assertEquals(3, callCount.get());
        assertEquals("initial...", result.state());
    }

    private SetterWithStatefulEffect<List<Integer>, Integer> createListSetter() {
        return new SetterWithStatefulEffect<>() {
            @Override
            public <State, Result> Stateful<Continuation<Result, List<Integer>>, State> modifyWithEffect(List<Integer> list, State initialState, java.util.function.BiFunction<Integer, State, Stateful<Continuation<Result, Integer>, State>> modifierWithEffect) {
                State currentState = initialState;
                List<Continuation<Result, Integer>> continuations = new ArrayList<>();
                boolean shortCircuited = false;
                for (Integer element : list) {
                    Stateful<Continuation<Result, Integer>, State> modified = modifierWithEffect.apply(element, currentState);
                    continuations.add(modified.value());
                    currentState = modified.state();
                }

                State finalState = currentState;
                Continuation<Result, List<Integer>> combinedContinuation = runner -> {
                    List<Integer> resultList = new ArrayList<>();
                    for (Continuation<Result, Integer> continuation : continuations) {
                        // This is tricky because we need to get back an Integer
                        // but continuation.run returns Result.
                        // In our tests, Result is List<Integer> and the runner we pass
                        // to Continuation.run is expected to return that.
                        @SuppressWarnings("unchecked")
                        Object value = continuation.run(v -> (Result) v);
                        resultList.add((Integer) value);
                    }
                    return runner.apply(resultList);
                };

                return Stateful.of(combinedContinuation, finalState);
            }
        };
    }
}
