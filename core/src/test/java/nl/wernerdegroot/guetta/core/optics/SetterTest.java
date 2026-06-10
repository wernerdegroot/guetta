package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetterTest {

    @Test
    void should_create_a_setter_from_a_streamer_and_a_collector() {
        Streamer<PokemonCard, PokemonCard.Attack> attacksStreamer = p -> p.attacks().stream();
        Setter<PokemonCard, PokemonCard.Attack> attacksSetter = Setter.from(attacksStreamer, Collectors.collectingAndThen(Collectors.toList(), (List<PokemonCard.Attack> attacks) -> {
            PokemonCard template = PokemonCard.bulbasaurWithWeaknessFactor(2);
            return new PokemonCard(template.name(), template.hp(), template.type(), attacks, template.weakness(), template.resistance(), template.retreatCost());
        }));

        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        PokemonCard updated = attacksSetter.modify(bulbasaur, attack -> new PokemonCard.Attack(attack.name().toUpperCase(), attack.damage(), attack.cost()));

        assertEquals("LEECH SEED", updated.attacks().get(0).name());
    }

    @Test
    void should_set_a_value_in_a_structure() {
        Setter<PokemonCard, Integer> hpSetter = (p, f) -> new PokemonCard(p.name(), f.apply(p.hp()), p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost());
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        PokemonCard updated = hpSetter.set(bulbasaur, 100);
        assertEquals(100, updated.hp());
    }

    @Test
    void should_return_a_unary_operator_that_sets_a_value() {
        Setter<PokemonCard, Integer> hpSetter = (p, f) -> new PokemonCard(p.name(), f.apply(p.hp()), p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost());
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        UnaryOperator<PokemonCard> setter = hpSetter.set(100);
        assertEquals(100, setter.apply(bulbasaur).hp());
    }

    @Test
    void should_return_a_unary_operator_that_modifies_a_value() {
        Setter<PokemonCard, Integer> hpSetter = (p, f) -> new PokemonCard(p.name(), f.apply(p.hp()), p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost());
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        UnaryOperator<PokemonCard> modifier = hpSetter.modify(hp -> hp + 10);
        assertEquals(80, modifier.apply(bulbasaur).hp());
    }

    @Test
    void should_compose_with_another_setter() {
        Setter<PokemonCard, PokemonCard.Weakness> weaknessSetter = (p, f) -> new PokemonCard(p.name(), p.hp(), p.type(), p.attacks(), f.apply(p.weakness()), p.resistance(), p.retreatCost());
        Setter<PokemonCard.Weakness, Integer> factorSetter = (w, f) -> new PokemonCard.Weakness(w.type(), f.apply(w.factor()));
        
        Setter<PokemonCard, Integer> weaknessFactorSetter = weaknessSetter.andThen(factorSetter);
        
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        PokemonCard updated = weaknessFactorSetter.set(bulbasaur, 3);
        
        assertEquals(3, updated.weakness().factor());
    }

    @Test
    void should_take_first_n_elements() {
        Setter<List<Integer>, Integer> listSetter = (list, modifier) -> list.stream().map(modifier).collect(Collectors.toList());
        List<Integer> input = List.of(1, 2, 3, 4, 5);
        
        Setter<List<Integer>, Integer> takeTwo = listSetter.take(2);
        List<Integer> result = takeTwo.modify(input, x -> x * 10);
        
        assertEquals(List.of(10, 20, 3, 4, 5), result);
    }

    @Test
    void should_take_elements_while_predicate_holds() {
        Setter<List<Integer>, Integer> listSetter = (list, modifier) -> list.stream().map(modifier).collect(Collectors.toList());
        List<Integer> input = List.of(1, 2, 3, 2, 1);
        
        Setter<List<Integer>, Integer> takeWhileLessThree = listSetter.takeWhile(x -> x < 3);
        List<Integer> result = takeWhileLessThree.modify(input, x -> x * 10);
        
        assertEquals(List.of(10, 20, 3, 2, 1), result);
    }

    @Test
    void should_drop_first_n_elements() {
        Setter<List<Integer>, Integer> listSetter = (list, modifier) -> list.stream().map(modifier).collect(Collectors.toList());
        List<Integer> input = List.of(1, 2, 3, 4, 5);
        
        Setter<List<Integer>, Integer> dropTwo = listSetter.drop(2);
        List<Integer> result = dropTwo.modify(input, x -> x * 10);
        
        assertEquals(List.of(1, 2, 30, 40, 50), result);
    }

    @Test
    void should_drop_elements_while_predicate_holds() {
        Setter<List<Integer>, Integer> listSetter = (list, modifier) -> list.stream().map(modifier).collect(Collectors.toList());
        List<Integer> input = List.of(1, 2, 3, 2, 1);
        
        Setter<List<Integer>, Integer> dropWhileLessThree = listSetter.dropWhile(x -> x < 3);
        List<Integer> result = dropWhileLessThree.modify(input, x -> x * 10);
        
        assertEquals(List.of(1, 2, 30, 20, 10), result);
    }
}
