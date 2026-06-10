package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetterTest {

    @Test
    void should_return_a_stream_containing_the_value() {
        Getter<PokemonCard, String> nameGetter = PokemonCard::name;
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        
        List<String> result = nameGetter.stream(bulbasaur).collect(Collectors.toList());
        assertEquals(List.of("Bulbasaur"), result);
    }

    @Test
    void should_compose_with_another_getter() {
        Getter<PokemonCard, PokemonCard.Weakness> weaknessGetter = PokemonCard::weakness;
        Getter<PokemonCard.Weakness, PokemonCard.Type> typeGetter = PokemonCard.Weakness::type;
        
        Getter<PokemonCard, PokemonCard.Type> weaknessTypeGetter = weaknessGetter.andThen(typeGetter);
        
        PokemonCard bulbasaur = PokemonCard.bulbasaurWithWeaknessFactor(2);
        assertEquals(PokemonCard.Type.FIRE, weaknessTypeGetter.get(bulbasaur));
    }
}
