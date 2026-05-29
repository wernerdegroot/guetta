package nl.wernerdegroot.guetta.core.optics.data;

import java.util.List;
import java.util.Optional;

/**
 * A simplified Pokémon Trading Card Game (TCG) Pokémon card.
 *
 * <p>A Pokémon card has:
 * <ul>
 *     <li>A name and hit points (HP)</li>
 *     <li>A Pokémon type, such as Fire or Grass</li>
 *     <li>One or more attacks that can be used during battle</li>
 *     <li>A weakness causing extra damage from a certain type</li>
 *     <li>Optionally a resistance reducing damage from a certain type</li>
 *     <li>A retreat cost paid to move the Pokémon from the Active Spot to the Bench</li>
 * </ul>
 *
 * <p>This model intentionally focuses on the most recognizable and composable mechanics of the TCG for use in optics examples and tests.
 */
public record PokemonCard(String name, int hp, Type type, List<Attack> attacks, Weakness weakness,
                          Optional<Resistance> resistance, List<Type> retreatCost) {

    /**
     * Pokémon elemental types.
     *
     * <p>Attacks, weaknesses, resistances and energy costs are based on types.
     */
    public enum Type {
        COLORLESS,
        GRASS,
        FIRE,
        WATER,
        ELECTRIC,
    }

    /**
     * A weakness causing attacks of a certain type to deal extra damage.
     *
     * <p>Modern Pokémon TCG cards typically use a factor of 2.
     */
    public record Weakness(Type type, int factor) {

    }

    /**
     * A resistance reducing damage from attacks of a certain type.
     *
     * <p>Modern Pokémon TCG cards commonly reduce damage by 30.
     */
    public record Resistance(Type type, int reduction) {

    }

    /**
     * An attack that can be used by a Pokémon during battle.
     *
     * <p>An attack has a name, a damage value and an energy cost.
     */
    public record Attack(String name, int damage, List<Type> cost) {

    }

    public static PokemonCard bulbasaurWithWeaknessFactor(int weaknessFactor) {
        return new PokemonCard(
                "Bulbasaur",
                70,
                Type.GRASS,
                List.of(
                        new Attack("Leech Seed", 20, List.of(Type.GRASS, Type.COLORLESS))
                ),
                new Weakness(
                        Type.FIRE,
                        weaknessFactor
                ),
                Optional.empty(),
                List.of(Type.COLORLESS, Type.COLORLESS)
        );
    }

    public static PokemonCard charmanderWithAttackDamage(int scratchDamage, int emberDamage) {
        return new PokemonCard(
                "Charmander",
                50,
                Type.FIRE,
                List.of(
                        new Attack(
                                "Scratch",
                                scratchDamage,
                                List.of(Type.COLORLESS)
                        ),
                        new Attack(
                                "Ember",
                                emberDamage,
                                List.of(Type.FIRE, Type.COLORLESS)
                        )
                ),
                new Weakness(
                        Type.WATER,
                        2
                ),
                Optional.empty(),
                List.of(Type.COLORLESS)
        );
    }
}
