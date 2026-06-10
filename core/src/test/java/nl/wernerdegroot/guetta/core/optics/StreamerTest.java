package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamerTest {

    @Test
    void should_create_a_streamer_from_a_getter_that_returns_a_collection() {
        Getter<PokemonCard, List<PokemonCard.Attack>> attacksGetter = PokemonCard::attacks;
        Streamer<PokemonCard, PokemonCard.Attack> attacksStreamer = Streamer.from(attacksGetter);

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);
        List<String> attackNames = attacksStreamer.stream(charmander)
                .map(PokemonCard.Attack::name)
                .collect(Collectors.toList());

        assertEquals(List.of("Scratch", "Ember"), attackNames);
    }

    @Test
    void should_create_a_streamer_that_returns_the_characters_of_a_string() {
        Streamer<String, Character> charactersStreamer = Streamer.characters();

        List<Character> chars = charactersStreamer.stream("Pika")
                .collect(Collectors.toList());

        assertEquals(List.of('P', 'i', 'k', 'a'), chars);
    }

    @Test
    void should_return_itself_as_a_streamer() {
        Streamer<String, Character> streamer = Streamer.characters();
        assertEquals(streamer, streamer.asStreamer());
    }

    @Test
    void should_compose_with_another_streamer() {
        Getter<PokemonCard, List<PokemonCard.Attack>> attacksGetter = PokemonCard::attacks;
        Streamer<PokemonCard, PokemonCard.Attack> attacksStreamer = Streamer.from(attacksGetter);
        Streamer<PokemonCard.Attack, Character> attackNameCharsStreamer = attack -> Streamer.characters().stream(attack.name());

        Streamer<PokemonCard, Character> attackNameChars = attacksStreamer.andThen(attackNameCharsStreamer);

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);
        String allChars = attackNameChars.stream(charmander)
                .map(Object::toString)
                .collect(Collectors.joining());

        assertEquals("ScratchEmber", allChars);
    }
}
