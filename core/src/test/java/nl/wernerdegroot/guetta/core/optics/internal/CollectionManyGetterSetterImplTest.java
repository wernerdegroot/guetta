package nl.wernerdegroot.guetta.core.optics.internal;

import nl.wernerdegroot.guetta.core.optics.ManyGetterSetter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static nl.wernerdegroot.guetta.core.optics.internal.CollectionManyGetterSetterImpl.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionManyGetterSetterImplTest {

    @Test
    void should_be_able_to_get_instance_for_known_concrete_class() {

        var collection = new ArrayList<Integer>();
        collection.add(1);
        collection.add(2);
        collection.add(3);

        ManyGetterSetter<ArrayList<Integer>, Integer> instance = getInstance(ArrayList.class);

        assertEquals(
                List.of(1, 2, 3),
                instance.stream(collection).toList()
        );

        assertEquals(
                List.of(2, 4, 6),
                instance.modify(collection, i -> i * 2)
        );
    }
}
