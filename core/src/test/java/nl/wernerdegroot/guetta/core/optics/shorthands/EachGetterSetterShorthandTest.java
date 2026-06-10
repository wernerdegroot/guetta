package nl.wernerdegroot.guetta.core.optics.shorthands;

import nl.wernerdegroot.guetta.core.optics.EachGetterSetter;
import nl.wernerdegroot.guetta.core.optics.data.PokemonCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EachGetterSetterShorthandTest {

    @Test
    void should_return_many_getter_setter_for_collection_attribute() {

        record Structure(Collection<Integer> value) { }

        var structure = new Structure(Set.of(1, 2, 3));

        EachGetterSetter<Structure, Integer> value = new EachGetterSetter<>() {
            @Override
            public Stream<Integer> stream(Structure s) {
                return s.value().stream();
            }

            @Override
            public Structure modify(Structure s, UnaryOperator<Integer> modifier) {
                return new Structure(s.value().stream().map(modifier).collect(Collectors.toList()));
            }
        };

        // Make sure `stream` works:
        assertEquals(
                List.of(1, 2, 3),
                value.stream(structure).sorted().toList()
        );

        // Make sure `modify` works:
        assertEquals(
                List.of(2, 4, 6),
                value.modify(structure, i -> i * 2).value().stream().sorted().toList()
        );
    }

    @Test
    void should_return_many_getter_setter_for_list_attribute() {

        record Structure(List<Integer> value) { }

        var structure = new Structure(List.of(1, 2, 3));

        EachGetterSetter<Structure, Integer> value = new EachGetterSetter<>() {
            @Override
            public Stream<Integer> stream(Structure s) {
                return s.value().stream();
            }

            @Override
            public Structure modify(Structure s, UnaryOperator<Integer> modifier) {
                return new Structure(s.value().stream().map(modifier).collect(Collectors.toList()));
            }
        };

        // Make sure `stream` works:
        assertEquals(
                List.of(1, 2, 3),
                value.stream(structure).toList()
        );

        // Make sure `modify` works:
        assertEquals(
                new Structure(List.of(2, 4, 6)),
                value.modify(structure, i -> i * 2)
        );
    }

    @Test
    void should_return_many_getter_setter_for_array_list_attribute() {

        record Structure(ArrayList<Integer> value) { }

        var structure = new Structure(new ArrayList<>(List.of(1, 2, 3)));

        EachGetterSetter<Structure, Integer> value = new EachGetterSetter<>() {
            @Override
            public Stream<Integer> stream(Structure s) {
                return s.value().stream();
            }

            @Override
            public Structure modify(Structure s, UnaryOperator<Integer> modifier) {
                return new Structure(new ArrayList<>(s.value().stream().map(modifier).collect(Collectors.toList())));
            }
        };

        // Make sure `stream` works:
        assertEquals(
                List.of(1, 2, 3),
                value.stream(structure).toList()
        );

        // Make sure `modify` works:
        assertEquals(
                new Structure(new ArrayList<>(List.of(2, 4, 6))),
                value.modify(structure, i -> i * 2)
        );
    }

    @Test
    void should_return_many_getter_setter_for_set_attribute() {

        record Structure(Set<Integer> value) { }

        var structure = new Structure(Set.of(1, 2, 3));

        EachGetterSetter<Structure, Integer> value = new EachGetterSetter<>() {
            @Override
            public Stream<Integer> stream(Structure s) {
                return s.value().stream();
            }

            @Override
            public Structure modify(Structure s, UnaryOperator<Integer> modifier) {
                return new Structure(s.value().stream().map(modifier).collect(Collectors.toSet()));
            }
        };

        // Make sure `stream` works:
        assertEquals(
                Set.of(1, 2, 3),
                value.stream(structure).collect(Collectors.toSet())
        );

        // Make sure `modify` works:
        assertEquals(
                new Structure(Set.of(2, 4, 6)),
                value.modify(structure, i -> i * 2)
        );
    }
}
