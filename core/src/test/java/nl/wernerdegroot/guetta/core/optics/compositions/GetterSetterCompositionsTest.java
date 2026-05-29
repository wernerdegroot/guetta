package nl.wernerdegroot.guetta.core.optics.compositions;

import nl.wernerdegroot.guetta.core.optics.Getter;
import nl.wernerdegroot.guetta.core.optics.GetterSetter;
import nl.wernerdegroot.guetta.core.optics.Streamer;
import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetterSetterCompositionsTest {

    @Test
    void composing_a_getter_setter_with_a_getter_returns_a_getter() {
        var bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);

        var weakness = GetterSetter.from(PokemonCard::weakness);
        var factor = Getter.from(PokemonCard.Weakness::factor);
        var weaknessFactor = weakness.andThen(factor);

        // Make sure `get` works:
        assertEquals(2, weaknessFactor.get(bulbasaur));
    }

    @Test
    void composing_a_getter_with_a_getter_setter_returns_a_getter() {
        var bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);

        var weakness = Getter.from(PokemonCard::weakness);
        var factor = GetterSetter.from(PokemonCard.Weakness::factor);
        var weaknessFactor = weakness.andThen(factor);

        // Make sure `get` works:
        assertEquals(2, weaknessFactor.get(bulbasaur));
    }

    @Test
    void composing_a_getter_setter_with_a_streamer_returns_a_streamer() {
        var bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);

        var name = GetterSetter.from(PokemonCard::name);
        var characters = Streamer.characters();
        var nameCharacters = name.andThen(characters);

        // Make sure `stream` works:
        assertEquals(List.of('B', 'u', 'l', 'b', 'a', 's', 'a', 'u', 'r'), nameCharacters.stream(bulbasaur).toList());
    }

    @Test
    void composing_a_streamer_with_a_getter_setter_returns_a_streamer() {
        var charmander = PokemonCard.charmanderWithAttackDamage(10, 30);

        var attack = Streamer.from(PokemonCard::attacks);
        var damage = GetterSetter.from(PokemonCard.Attack::damage);
        var attackDamage = attack.andThen(damage);

        // Make sure `stream` works:
        assertEquals(List.of(10, 30), attackDamage.stream(charmander).toList());
    }

    @Test
    void composing_a_getter_setter_with_a_getter_setter_returns_a_getter_setter() {
        var bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);

        var weakness = GetterSetter.from(PokemonCard::weakness);
        var factor = GetterSetter.from(PokemonCard.Weakness::factor);
        var weaknessFactor = weakness.andThen(factor);

        // Make sure `get` works:
        assertEquals(2, weaknessFactor.get(bulbasaur));

        // Make sure `set` works:
        assertEquals(PokemonCard.bulbasaurWithWeaknessFactor(3), weaknessFactor.set(bulbasaur, 3));

        // Make sure `modify` works:
        assertEquals(PokemonCard.bulbasaurWithWeaknessFactor(4), weaknessFactor.modify(bulbasaur, value -> value * 2));
    }
}
