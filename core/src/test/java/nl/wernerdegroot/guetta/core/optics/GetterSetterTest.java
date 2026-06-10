package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetterSetterTest {
    
    private final GetterSetter<PokemonCard, Integer> hpGS = GetterSetter.from(
            PokemonCard::hp,
            (p, modifier) -> new PokemonCard(p.name(), modifier.apply(p.hp()), p.type(), p.attacks(), p.weakness(), p.resistance(), p.retreatCost())
    );

    private final GetterSetter<PokemonCard, PokemonCard.Weakness> weaknessGS = GetterSetter.from(
            PokemonCard::weakness,
            (p, modifier) -> new PokemonCard(p.name(), p.hp(), p.type(), p.attacks(), modifier.apply(p.weakness()), p.resistance(), p.retreatCost())
    );

    private final GetterSetter<PokemonCard.Weakness, Integer> factorGS = GetterSetter.from(
            PokemonCard.Weakness::factor,
            (w, modifier) -> new PokemonCard.Weakness(w.type(), modifier.apply(w.factor()))
    );

    @Test
    void should_create_a_getter_setter_from_a_getter_and_a_setter() {
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(70, hpGS.get(bulbasaur));

        PokemonCard updated = hpGS.set(bulbasaur, 100);
        assertEquals(100, updated.hp());
    }

    @Test
    void should_compose_with_another_getter_setter() {
        GetterSetter<PokemonCard, Integer> weaknessFactorGS = weaknessGS.andThen(factorGS);

        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(2, weaknessFactorGS.get(bulbasaur));

        PokemonCard updated = weaknessFactorGS.set(bulbasaur, 3);
        assertEquals(3, updated.weakness().factor());
    }
}
