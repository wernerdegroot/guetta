package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EachGetterSetterWithEffectTest {

    private final EachGetterSetterWithEffect<PokemonCard, PokemonCard.Attack> attack = new EachGetterSetterWithEffect<>() {
        @Override
        public Stream<PokemonCard.Attack> stream(PokemonCard structure) {
            return structure.attacks().stream();
        }

        @Override
        public PokemonCard modify(PokemonCard pokemonCard, UnaryOperator<PokemonCard.Attack> modifier) {
            List<PokemonCard.Attack> attacks = stream(pokemonCard).map(modifier).toList();
            return pokemonCardWithAttacks(pokemonCard, attacks);
        }

        @Override
        public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard pokemonCard, Function<PokemonCard.Attack, Continuation<Result, PokemonCard.Attack>> modifier) {
            Continuation<Result, List<PokemonCard.Attack>> resultContinuation = Continuation.of(new ArrayList<>());

            for (PokemonCard.Attack attack : pokemonCard.attacks()) {
                resultContinuation = resultContinuation.flatMap(result ->
                        modifier.apply(attack).map(modifiedAttack -> {
                            List<PokemonCard.Attack> copy = new ArrayList<>(result);
                            copy.add(modifiedAttack);
                            return copy;
                        })
                );
            }

            return resultContinuation.map(attacks -> pokemonCardWithAttacks(pokemonCard, attacks));
        }

        private static PokemonCard pokemonCardWithAttacks(PokemonCard structure, List<PokemonCard.Attack> attacks) {
            return new PokemonCard(structure.name(), structure.hp(), structure.type(), attacks, structure.weakness(), structure.resistance(), structure.retreatCost());
        }
    };

    private final EachGetterSetterWithEffect<PokemonCard.Attack, PokemonCard.Type> cost = new EachGetterSetterWithEffect<>() {
        @Override
        public Stream<PokemonCard.Type> stream(PokemonCard.Attack attack) {
            return attack.cost().stream();
        }

        @Override
        public PokemonCard.Attack modify(PokemonCard.Attack attack, UnaryOperator<PokemonCard.Type> modifier) {
            List<PokemonCard.Type> cost = stream(attack).map(modifier).toList();
            return attackWithCost(attack, cost);
        }

        @Override
        public <Result> Continuation<Result, PokemonCard.Attack> modifyWithEffect(PokemonCard.Attack attack, Function<PokemonCard.Type, Continuation<Result, PokemonCard.Type>> modifier) {
            Continuation<Result, List<PokemonCard.Type>> resultContinuation = Continuation.of(new ArrayList<>());

            for (PokemonCard.Type type : attack.cost()) {
                resultContinuation = resultContinuation.flatMap(result ->
                        modifier.apply(type).map(modifiedType -> {
                            List<PokemonCard.Type> copy = new ArrayList<>(result);
                            copy.add(modifiedType);
                            return copy;
                        })
                );
            }

            return resultContinuation.map(cost -> attackWithCost(attack, cost));
        }

        private static PokemonCard.Attack attackWithCost(PokemonCard.Attack attack, List<PokemonCard.Type> cost) {
            return new PokemonCard.Attack(attack.name(), attack.damage(), cost);
        }
    };

    @Test
    void should_create_an_each_getter_setter_with_effect_from_a_class() {
        EachGetterSetterWithEffect<List<PokemonCard.Attack>, PokemonCard.Attack> eachAttackFromClass = EachGetterSetterWithEffect.from(ArrayList.class);

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);
        List<PokemonCard.Attack> attacks = new ArrayList<>(charmander.attacks());
        List<PokemonCard.Attack> updated = eachAttackFromClass.modifyWithIdentity(attacks, attack -> new PokemonCard.Attack(attack.name().toUpperCase(), attack.damage(), attack.cost()));

        assertEquals("SCRATCH", updated.get(0).name());
        assertEquals("EMBER", updated.get(1).name());
    }

    @Test
    void should_compose_with_another_each_getter_setter_with_effect() {
        EachGetterSetterWithEffect<PokemonCard, PokemonCard.Type> composed = attack.andThen(cost);

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);

        PokemonCard updated = composed.modify(charmander, type -> type == PokemonCard.Type.FIRE ? PokemonCard.Type.GRASS : type);

        assertEquals(PokemonCard.Type.GRASS, updated.attacks().get(1).cost().get(0));
    }
}
