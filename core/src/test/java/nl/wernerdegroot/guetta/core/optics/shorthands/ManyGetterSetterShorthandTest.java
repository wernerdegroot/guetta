package nl.wernerdegroot.guetta.core.optics.shorthands;

import nl.wernerdegroot.guetta.core.optics.ManyGetterSetter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyGetterSetterShorthandTest {

    @Test
    void should_return_many_getter_setter_for_list_attribute() {

        record Structure(List<Integer> value) { }

        var structure = new Structure(List.of(1, 2, 3));

        var value = ManyGetterSetter.from(Structure::value);

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

        var value = ManyGetterSetter.from(Structure::value);

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

        var value = ManyGetterSetter.from(Structure::value);

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
