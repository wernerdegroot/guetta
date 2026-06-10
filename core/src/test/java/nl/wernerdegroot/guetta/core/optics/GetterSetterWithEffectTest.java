package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetterSetterWithEffectTest {

    private final GetterSetterWithEffect<PokemonCard, Integer> hpGSWE = GetterSetterWithEffect.from(
            PokemonCard::hp,
            new SetterWithEffect<>() {
                @Override
                public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard p, Function<Integer, Continuation<Result, Integer>> modifier) {
                    return modifier.apply(p.hp()).map(hp -> new PokemonCard(p.name(), hp, p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost()));
                }
            }
    );

    private final GetterSetterWithEffect<PokemonCard, PokemonCard.Weakness> weaknessGSWE = GetterSetterWithEffect.from(
            PokemonCard::weakness,
            new SetterWithEffect<>() {
                @Override
                public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard p, Function<PokemonCard.Weakness, Continuation<Result, PokemonCard.Weakness>> modifier) {
                    return modifier.apply(p.weakness()).map(w -> new PokemonCard(p.name(), p.hp(), p.type(), p.attacks(), w, p.resistance(), p.retreatCost()));
                }
            }
    );

    private final GetterSetterWithEffect<PokemonCard.Weakness, Integer> factorGSWE = GetterSetterWithEffect.from(
            PokemonCard.Weakness::factor,
            new SetterWithEffect<>() {
                @Override
                public <Result> Continuation<Result, PokemonCard.Weakness> modifyWithEffect(PokemonCard.Weakness w, Function<Integer, Continuation<Result, Integer>> modifier) {
                    return modifier.apply(w.factor()).map(f -> new PokemonCard.Weakness(w.type(), f));
                }
            }
    );

    @Test
    void should_create_a_getter_setter_with_effect_from_a_getter_and_a_setter_with_effect() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(70, hpGSWE.get(bulbasaur));
        
        Optional<PokemonCard> updated = hpGSWE.modifyWithOptional(bulbasaur, hp -> Optional.of(hp + 10));
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }

    @Test
    void should_compose_with_another_getter_setter_with_effect() {
        GetterSetterWithEffect<PokemonCard, Integer> weaknessFactorGSWE = weaknessGSWE.andThen(factorGSWE);
        
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        Optional<PokemonCard> updated = weaknessFactorGSWE.modifyWithOptional(bulbasaur, f -> Optional.of(f + 1));
        
        assertTrue(updated.isPresent());
        assertEquals(3, updated.get().weakness().factor());
    }
}
