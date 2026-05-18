package nl.wernerdegroot.guetta.core.optics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetterSetterTest {

    public record Person(String name, int age) {}

    @Test
    void ofWithValidRecord() {
        NamedGetterSetter<Person, String> nameGetterSetter = GetterSetter.of(Person::name);

        Person person = new Person("John", 30);

        assertEquals("John", nameGetterSetter.get(person));
        assertEquals("name", nameGetterSetter.getName());

        Person updated = nameGetterSetter.set(person, "Jane");
        assertEquals("Jane", updated.name());
        assertEquals(30, updated.age());
    }

    @Test
    void ofWithNullThrowsException() {
        assertThrows(NullPointerException.class, () -> GetterSetter.of(null));
    }

    @Test
    void ofWithNonMethodReferenceThrowsException() {
        // A lambda that is not a method reference might not have the expected SerializedLambda structure
        // Although SerializableFunction is a functional interface, NamedMethod.from expects a method reference.
        SerializableFunction<Person, String> fn = (Person p) -> p.name() + "!";
        assertThrows(RuntimeException.class, () -> GetterSetter.of(fn));
    }

    @Test
    void setWithNullRecordThrowsException() {
        NamedGetterSetter<Person, String> nameGetterSetter = GetterSetter.of(Person::name);
        assertThrows(NullPointerException.class, () -> nameGetterSetter.set(null, "Jane"));
    }

    @Test
    void getWithNullRecordThrowsException() {
        NamedGetterSetter<Person, String> nameGetterSetter = GetterSetter.of(Person::name);
        assertThrows(NullPointerException.class, () -> nameGetterSetter.get(null));
    }

    @Test
    void ofWithNonRecordMethodReferenceThrowsException() {
        class NotARecord {
            public String name() {
                return "name";
            }
        }

        SerializableFunction<NotARecord, String> fn = NotARecord::name;
        assertThrows(IllegalArgumentException.class, () -> GetterSetter.of(fn));
    }

    @Test
    void ofWithNonRecordComponentMethodReferenceThrowsException() {
        record SomeRecord(String name) {
            public String notAComponent() {
                return "not";
            }
        }

        assertThrows(IllegalArgumentException.class, () -> GetterSetter.of(SomeRecord::notAComponent));
    }

    @Test
    void getWithWrongRecordTypeThrowsException() {
        NamedGetterSetter<Person, String> nameGetterSetter = GetterSetter.of(Person::name);

        record AnotherPerson(String name) {}
        AnotherPerson another = new AnotherPerson("Jane");

        assertThrows(IllegalArgumentException.class, () -> {
            @SuppressWarnings("unchecked")
            var nameGetterSetterRaw = (NamedGetterSetter) nameGetterSetter;
            nameGetterSetterRaw.get(another);
        });
    }
}
