package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedGetterSetterWithEffectTest {

    @Test
    void should_create_a_named_getter_setter_with_effect_from_a_name_a_getter_and_a_setter_with_effect() {
        Getter<PokemonCard, Integer> getter = PokemonCard::hp;
        SetterWithEffect<PokemonCard, Integer> setterWithEffect = new SetterWithEffect<>() {
            @Override
            public <Result> Continuation<Result, PokemonCard> modifyWithEffect(PokemonCard structure, Function<Integer, Continuation<Result, Integer>> modifierWithEffect) {
                return modifierWithEffect.apply(structure.hp()).map(newHp -> new PokemonCard(structure.name(), newHp, structure.type(), structure.attacks(), structure.weakness(), structure.resistance(), structure.retreatCost()));
            }
        };

        NamedGetterSetterWithEffect<PokemonCard, Integer> hpNamedGSWE = NamedGetterSetterWithEffect.from("hp", getter, setterWithEffect);

        assertEquals("hp", hpNamedGSWE.name());
        
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(70, hpNamedGSWE.get(bulbasaur));
        
        Optional<PokemonCard> updated = hpNamedGSWE.modifyWithOptional(bulbasaur, hp -> Optional.of(hp + 10));
        assertTrue(updated.isPresent());
        assertEquals(80, updated.get().hp());
    }
}
