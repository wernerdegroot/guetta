package nl.wernerdegroot.guetta.core.optics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NamedTest {

    @Test
    void should_return_the_name() {
        Named named = () -> "Pikachu";
        assertEquals("Pikachu", named.name());
    }
}
