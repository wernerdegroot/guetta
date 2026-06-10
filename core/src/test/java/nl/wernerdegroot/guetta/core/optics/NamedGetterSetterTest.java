package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedGetterSetterTest {

    @Test
    void should_create_a_named_getter_setter_from_a_name_a_getter_and_a_setter() {
        Getter<PokemonCard, Integer> getter = PokemonCard::hp;
        Setter<PokemonCard, Integer> setter = (p, f) -> new PokemonCard(p.name(), f.apply(p.hp()), p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost());

        NamedGetterSetter<PokemonCard, Integer> hpNamedGS = NamedGetterSetter.from("hp", getter, setter);

        assertEquals("hp", hpNamedGS.name());
        
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(70, hpNamedGS.get(bulbasaur));
        
        PokemonCard updated = hpNamedGS.set(bulbasaur, 100);
        assertEquals(100, updated.hp());
    }
}
