package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EachGetterSetterTest {

    @Test
    void should_create_an_each_getter_setter_from_a_streamer_and_a_collector() {
        EachGetterSetter<List<PokemonCard.Attack>, PokemonCard.Attack> eachAttack = EachGetterSetter.from(List::stream, Collectors.toCollection(ArrayList::new));

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);
        List<PokemonCard.Attack> updatedAttacks = eachAttack.modify(charmander.attacks(), attack -> new PokemonCard.Attack(attack.name().toUpperCase(), attack.damage(), attack.cost()));

        assertEquals("SCRATCH", updatedAttacks.get(0).name());
        assertEquals("EMBER", updatedAttacks.get(1).name());
    }

    @Test
    void should_compose_with_another_each_getter_setter() {
        EachGetterSetter<List<PokemonCard.Attack>, PokemonCard.Attack> eachAttack = EachGetterSetter.from(List::stream, Collectors.toCollection(ArrayList::new));
        EachGetterSetter<PokemonCard.Attack, PokemonCard.Type> eachCostType = EachGetterSetter.from(attack -> attack.cost().stream(), Collectors.collectingAndThen(Collectors.toList(), (List<PokemonCard.Type> cost) -> new PokemonCard.Attack("test", 0, cost)));
        
        // This composition is a bit synthetic because of the collector in eachCostType
        EachGetterSetter<List<PokemonCard.Attack>, PokemonCard.Type> eachAttackCostType = eachAttack.andThen(eachCostType);

        PokemonCard charmander = PokemonCard.charmanderWithAttackDamage(10, 30);
        List<PokemonCard.Type> allTypes = eachAttackCostType.stream(charmander.attacks()).collect(Collectors.toList());
        
        // Charmander's Scratch cost: [COLORLESS], Ember cost: [FIRE, COLORLESS]
        assertEquals(List.of(PokemonCard.Type.COLORLESS, PokemonCard.Type.FIRE, PokemonCard.Type.COLORLESS), allTypes);
    }
}
