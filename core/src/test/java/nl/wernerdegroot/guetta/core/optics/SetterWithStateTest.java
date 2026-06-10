package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.Stateful;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetterWithStateTest {

    @Test
    void take_should_only_modify_first_n_elements() {
        // A simple SetterWithState for a List
        SetterWithState<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        AtomicInteger callCount = new AtomicInteger(0);

        // Take only 2
        SetterWithState<List<Integer>, Integer> takeTwo = listSetter.take(2);

        Stateful<List<Integer>, String> result = takeTwo.modifyWithState(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(val * 10, state + ".");
        });

        // Elements 1 and 2 should be modified (10, 20).
        // Elements 3, 4, 5 should NOT be modified.
        assertEquals(List.of(10, 20, 3, 4, 5), result.value());

        // The modifier should only be called for elements that are actually being modified.
        // For take(2), that's the first 2 elements.
        assertEquals(2, callCount.get(), "Modifier should only be called for the first 2 elements");

        // What about the final state?
        // For the first 2 elements, state becomes "initial.."
        // For the remaining 3 elements, the modifier is NOT called, so state remains "initial.."
        assertEquals("initial..", result.state());
    }

    @Test
    void take_zero_should_modify_nothing() {
        SetterWithState<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3);
        AtomicInteger callCount = new AtomicInteger(0);
        SetterWithState<List<Integer>, Integer> takeZero = listSetter.take(0);

        Stateful<List<Integer>, String> result = takeZero.modifyWithState(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(val * 10, state + ".");
        });

        assertEquals(List.of(1, 2, 3), result.value());
        assertEquals(0, callCount.get());
        assertEquals("initial", result.state());
    }

    @Test
    void drop_should_only_modify_elements_after_n() {
        SetterWithState<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 4, 5);
        AtomicInteger callCount = new AtomicInteger(0);

        // Drop first 2
        SetterWithState<List<Integer>, Integer> dropTwo = listSetter.drop(2);

        Stateful<List<Integer>, String> result = dropTwo.modifyWithState(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(val * 10, state + ".");
        });

        assertEquals(List.of(1, 2, 30, 40, 50), result.value());
        assertEquals(3, callCount.get());
        assertEquals("initial...", result.state());
    }

    @Test
    void takeWhile_should_only_modify_while_predicate_holds() {
        SetterWithState<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 2, 1);
        AtomicInteger callCount = new AtomicInteger(0);

        // Take while < 3
        SetterWithState<List<Integer>, Integer> takeWhileLessThree = listSetter.takeWhile(val -> val < 3);

        Stateful<List<Integer>, String> result = takeWhileLessThree.modifyWithState(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(val * 10, state + ".");
        });

        assertEquals(List.of(10, 20, 3, 2, 1), result.value());
        assertEquals(2, callCount.get());
        assertEquals("initial..", result.state());
    }

    @Test
    void dropWhile_should_only_modify_after_predicate_fails() {
        SetterWithState<List<Integer>, Integer> listSetter = createListSetter();

        List<Integer> input = List.of(1, 2, 3, 2, 1);
        AtomicInteger callCount = new AtomicInteger(0);

        // Drop while < 3
        SetterWithState<List<Integer>, Integer> dropWhileLessThree = listSetter.dropWhile(val -> val < 3);

        Stateful<List<Integer>, String> result = dropWhileLessThree.modifyWithState(input, "initial", (val, state) -> {
            callCount.incrementAndGet();
            return Stateful.of(val * 10, state + ".");
        });

        assertEquals(List.of(1, 2, 30, 20, 10), result.value());
        assertEquals(3, callCount.get());
        assertEquals("initial...", result.state());
    }


    private SetterWithState<List<Integer>, Integer> createListSetter() {
        return new SetterWithState<>() {
            @Override
            public <State> Stateful<List<Integer>, State> modifyWithState(List<Integer> list, State initialState, java.util.function.BiFunction<Integer, State, Stateful<Integer, State>> modifierWithState) {
                State currentState = initialState;
                List<Integer> result = new ArrayList<>();
                for (Integer element : list) {
                    Stateful<Integer, State> modified = modifierWithState.apply(element, currentState);
                    result.add(modified.value());
                    currentState = modified.state();
                }
                return Stateful.of(result, currentState);
            }
        };
    }
}