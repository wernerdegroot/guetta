package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SetterWithEffectTest {

    private final SetterWithEffect<PokemonCard, Integer> hpSetterWithEffect = new SetterWithEffect<>() {
        @Override
        public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard p, Function<Integer, Continuation<Result, Integer>> modifier) {
            return modifier.apply(p.hp()).map(hp -> new PokemonCard(p.name(), hp, p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost()));
        }
    };

    private final SetterWithEffect<PokemonCard, PokemonCard.Weakness> weaknessSetter = new SetterWithEffect<>() {
        @Override
        public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard p, Function<PokemonCard.Weakness, Continuation<Result, PokemonCard.Weakness>> modifier) {
            return modifier.apply(p.weakness()).map(w -> new PokemonCard(p.name(), p.hp(), p.type(), p.attacks(), w, p.resistance(), p.retreatCost()));
        }
    };

    private final SetterWithEffect<PokemonCard.Weakness, Integer> factorSetter = new SetterWithEffect<>() {
        @Override
        public <Result> Continuation<Result, PokemonCard.Weakness> modifyWithEffect(PokemonCard.Weakness w, Function<Integer, Continuation<Result, Integer>> modifier) {
            return modifier.apply(w.factor()).map(f -> new PokemonCard.Weakness(w.type(), f));
        }
    };

    @Test
    void should_return_itself_as_a_setter_with_effect() {
        assertEquals(hpSetterWithEffect, hpSetterWithEffect.asSetterWithEffect());
    }

    @Test
    void should_set_a_value_with_an_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Continuation<Optional<PokemonCard>, Integer> valueWithEffect = Continuation.optional(Optional.of(100));
        
        Optional<PokemonCard> updated = hpSetterWithEffect.setWithEffect(bulbasaur, valueWithEffect).run(Optional::of);
        
        assertTrue(updated.isPresent());
        assertEquals(100, updated.get().hp());
    }

    @Test
    void should_modify_a_value_with_an_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        Optional<PokemonCard> updated = hpSetterWithEffect.modifyWithEffect(bulbasaur, hp -> Continuation.optional(Optional.of(hp + 10)), Optional::of);
        
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_return_a_function_that_sets_a_value_with_an_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Continuation<Optional<PokemonCard>, Integer> valueWithEffect = Continuation.optional(Optional.of(100));
        
        Function<PokemonCard, Continuation<Optional<PokemonCard>, PokemonCard>> setter = hpSetterWithEffect.setWithEffect(valueWithEffect);
        Optional<PokemonCard> updated = setter.apply(bulbasaur).run(Optional::of);
        
        assertTrue(updated.isPresent());
        assertEquals(100, updated.get().hp());
    }

    @Test
    void should_return_a_function_that_modifies_a_value_with_an_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Function<Integer, Continuation<Optional<PokemonCard>, Integer>> modifierWithEffect = hp -> Continuation.optional(Optional.of(hp + 10));
        
        Function<PokemonCard, Continuation<Optional<PokemonCard>, PokemonCard>> modifier = hpSetterWithEffect.modifyWithEffect(modifierWithEffect);
        Optional<PokemonCard> updated = modifier.apply(bulbasaur).run(Optional::of);
        
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_modify_a_value_with_an_effect_using_a_runner() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Function<Integer, Continuation<Optional<PokemonCard>, Integer>> modifierWithEffect = hp -> Continuation.optional(Optional.of(hp + 10));
        
        Optional<PokemonCard> updated = hpSetterWithEffect.modifyWithEffect(bulbasaur, modifierWithEffect, Optional::of);
        
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_return_a_function_that_modifies_a_value_with_an_effect_using_a_runner() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Function<Integer, Continuation<Optional<PokemonCard>, Integer>> modifierWithEffect = hp -> Continuation.optional(Optional.of(hp + 10));
        
        Function<PokemonCard, Optional<PokemonCard>> modifier = hpSetterWithEffect.modifyWithEffect(modifierWithEffect, Optional::of);
        Optional<PokemonCard> updated = modifier.apply(bulbasaur);
        
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_compose_with_another_setter_with_effect() {
        SetterWithEffect<PokemonCard, Integer> weaknessFactorSetter = weaknessSetter.andThen(factorSetter);
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        Optional<PokemonCard> updated = weaknessFactorSetter.modifyWithOptional(bulbasaur, f -> Optional.of(f + 1));
        
        assertTrue(updated.isPresent());
        assertEquals(3, updated.get().weakness().factor());
    }

    @Test
    void should_modify_a_value_with_the_identity_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        PokemonCard updated = hpSetterWithEffect.modifyWithIdentity(bulbasaur, hp -> hp + 10);
        assertEquals(80, updated.hp());
    }

    @Test
    void should_modify_a_value_with_the_nullable_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        PokemonCard updated = hpSetterWithEffect.modifyWithNullable(bulbasaur, hp -> hp > 0 ? hp + 10 : null);
        assertEquals(80, updated.hp());
        
        PokemonCard updatedNull = hpSetterWithEffect.modifyWithNullable(bulbasaur, hp -> null);
        assertNull(updatedNull);
    }

    @Test
    void should_modify_a_value_with_the_optional_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Optional<PokemonCard> updated = hpSetterWithEffect.modifyWithOptional(bulbasaur, hp -> Optional.of(hp + 10));
        assertEquals(80, updated.get().hp());
        
        Optional<PokemonCard> updatedEmpty = hpSetterWithEffect.modifyWithOptional(bulbasaur, hp -> Optional.empty());
        assertFalse(updatedEmpty.isPresent());
    }

    @Test
    void should_modify_a_value_with_the_completable_future_effect() throws ExecutionException, InterruptedException {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        CompletableFuture<PokemonCard> updated = hpSetterWithEffect.modifyWithCompletableFuture(bulbasaur, hp -> CompletableFuture.completedFuture(hp + 10));
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_modify_a_value_with_the_stream_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        List<PokemonCard> updated = hpSetterWithEffect.modifyWithStream(bulbasaur, hp -> Stream.of(hp + 10, hp + 20)).collect(Collectors.toList());
        assertEquals(2, updated.size());
        assertEquals(80, updated.get(0).hp());
        assertEquals(90, updated.get(1).hp());
    }

    @Test
    void should_modify_a_value_with_the_list_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        List<PokemonCard> updated = hpSetterWithEffect.modifyWithList(bulbasaur, hp -> List.of(hp + 10, hp + 20));
        assertEquals(2, updated.size());
        assertEquals(80, updated.get(0).hp());
        assertEquals(90, updated.get(1).hp());
    }

    @Test
    void should_only_modify_the_first_n_values_when_using_take_expecting_just() {
        EachGetterSetterWithEffect<List<Integer>, Integer> each = EachGetterSetterWithEffect.from(ArrayList.class);
        List<Integer> values = List.of(1, 2, 3);

        Optional<List<Integer>> updated = each.take(2).modifyWithOptional(values, v -> v == 3 ? Optional.empty() : Optional.of(v * 10));

        assertTrue(updated.isPresent());
        assertEquals(List.of(10, 20, 3), updated.get());
    }

    @Test
    void should_only_modify_the_first_n_values_when_using_take_expecting_none() {
        EachGetterSetterWithEffect<List<Integer>, Integer> each = EachGetterSetterWithEffect.from(ArrayList.class);
        List<Integer> values = List.of(1, 2, 3);

        // Modification of the second element fails
        Optional<List<Integer>> updated = each.take(2).modifyWithOptional(values, v -> v == 2 ? Optional.empty() : Optional.of(v * 10));

        assertFalse(updated.isPresent());
    }

    @Test
    void should_work_with_composed_setter_and_take_spanning_multiple_structures() {
        // List<List<Integer>> -> List<Integer>
        EachGetterSetterWithEffect<List<List<Integer>>, List<Integer>> eachList = EachGetterSetterWithEffect.from(ArrayList.class);
        // List<Integer> -> Integer
        EachGetterSetterWithEffect<List<Integer>, Integer> eachInt = EachGetterSetterWithEffect.from(ArrayList.class);

        // List<List<Integer>> -> Integer
        EachGetterSetterWithEffect<List<List<Integer>>, Integer> composed = eachList.andThen(eachInt);

        List<List<Integer>> values = List.of(
                List.of(1, 2),
                List.of(3, 4)
        );

        // Take 3 elements from the composed traversal.
        // It should take:
        // 1. (from [1, 2]) -> 1
        // 2. (from [1, 2]) -> 2
        // 3. (from [3, 4]) -> 3
        // 4. (from [3, 4]) -> 4 (should be left alone)

        Optional<List<List<Integer>>> updated = composed.take(3).modifyWithOptional(values, v -> Optional.of(v * 10));

        assertTrue(updated.isPresent());
        List<List<Integer>> expected = List.of(
                List.of(10, 20),
                List.of(30, 4)
        );
        assertEquals(expected, updated.get());
    }

    @Test
    void should_only_modify_values_while_predicate_is_true() {
        EachGetterSetterWithEffect<List<Integer>, Integer> each = EachGetterSetterWithEffect.from(ArrayList.class);
        List<Integer> values = List.of(1, 2, 3, 4, 5);

        // Modify while value is less than 4
        Optional<List<Integer>> updated = each.takeWhile(v -> v < 4).modifyWithOptional(values, v -> Optional.of(v * 10));

        assertTrue(updated.isPresent());
        assertEquals(List.of(10, 20, 30, 4, 5), updated.get());
    }

    @Test
    void should_stop_modifying_after_first_predicate_failure() {
        EachGetterSetterWithEffect<List<Integer>, Integer> each = EachGetterSetterWithEffect.from(ArrayList.class);
        List<Integer> values = List.of(1, 4, 2, 3);

        // Modify while value is less than 4
        // 1 -> < 4 (true) -> 10
        // 4 -> < 4 (false) -> 4 (stop modifying)
        // 2 -> skip
        // 3 -> skip
        Optional<List<Integer>> updated = each.takeWhile(v -> v < 4).modifyWithOptional(values, v -> Optional.of(v * 10));

        assertTrue(updated.isPresent());
        assertEquals(List.of(10, 4, 2, 3), updated.get());
    }
}
