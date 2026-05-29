package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.ManyGetterSetterImpl;
import nl.wernerdegroot.guetta.core.optics.internal.NamedMethod;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class ManyGetterSetterRegistry {

    private static final Map<Class<?>, ManyGetterSetter<?, ?>> MANY_GETTER_SETTERS_BY_CLASS;

    static {

        // The following is inspired by MapStruct: https://mapstruct.org/documentation/stable/reference/html/#implementation-types-for-collection-mappings

        MANY_GETTER_SETTERS_BY_CLASS = new HashMap<>();

        // Interfaces
        MANY_GETTER_SETTERS_BY_CLASS.put(Collection.class, ManyGetterSetter.from(Collection::stream, toCollection()));
        MANY_GETTER_SETTERS_BY_CLASS.put(List.class, ManyGetterSetter.from(List::stream, toList()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Set.class, ManyGetterSetter.from(Set::stream, toSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(SortedSet.class, ManyGetterSetter.from(SortedSet::stream, toSortedSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(NavigableSet.class, ManyGetterSetter.from(NavigableSet::stream, toNavigableSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Queue.class, ManyGetterSetter.from(Queue::stream, toQueue()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Deque.class, ManyGetterSetter.from(Deque::stream, toDeque()));

        // List implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(ArrayList.class, ManyGetterSetter.from(ArrayList::stream, Collectors.toCollection(ArrayList::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedList.class, ManyGetterSetter.from(LinkedList::stream, Collectors.toCollection(LinkedList::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(Vector.class, ManyGetterSetter.from(Vector::stream, Collectors.toCollection(Vector::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(Stack.class, ManyGetterSetter.from(Stack::stream, Collectors.toCollection(Stack::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(CopyOnWriteArrayList.class, ManyGetterSetter.from(CopyOnWriteArrayList::stream, Collectors.toCollection(CopyOnWriteArrayList::new)));

        // Set implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(HashSet.class, ManyGetterSetter.from(HashSet::stream, Collectors.toCollection(HashSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedHashSet.class, ManyGetterSetter.from(LinkedHashSet::stream, Collectors.toCollection(LinkedHashSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(TreeSet.class, ManyGetterSetter.from(TreeSet::stream, Collectors.toCollection(TreeSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(CopyOnWriteArraySet.class, ManyGetterSetter.from(CopyOnWriteArraySet::stream, Collectors.toCollection(CopyOnWriteArraySet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentSkipListSet.class, ManyGetterSetter.from(ConcurrentSkipListSet::stream, Collectors.toCollection(ConcurrentSkipListSet::new)));

        // Queue / Deque implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(PriorityQueue.class, ManyGetterSetter.from(PriorityQueue::stream, Collectors.toCollection(PriorityQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ArrayDeque.class, ManyGetterSetter.from(ArrayDeque::stream, Collectors.toCollection(ArrayDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentLinkedQueue.class, ManyGetterSetter.from(ConcurrentLinkedQueue::stream, Collectors.toCollection(ConcurrentLinkedQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentLinkedDeque.class, ManyGetterSetter.from(ConcurrentLinkedDeque::stream, Collectors.toCollection(ConcurrentLinkedDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedBlockingQueue.class, ManyGetterSetter.from(LinkedBlockingQueue::stream, Collectors.toCollection(LinkedBlockingQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedBlockingDeque.class, ManyGetterSetter.from(LinkedBlockingDeque::stream, Collectors.toCollection(LinkedBlockingDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(PriorityBlockingQueue.class, ManyGetterSetter.from(PriorityBlockingQueue::stream, Collectors.toCollection(PriorityBlockingQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedTransferQueue.class, ManyGetterSetter.from(LinkedTransferQueue::stream, Collectors.toCollection(LinkedTransferQueue::new)));

        // The following are omitted because they cannot easily be collected into without some extra configuration:
        // - EnumSet
        // - ArrayBlockingQueue
    }

    private static <T> Collector<T, ?, Collection<T>> toCollection() {
        return Collectors.toCollection(ArrayList::new);
    }

    private static <T> Collector<T, ?, List<T>> toList() {
        return Collectors.toCollection(ArrayList::new);
    }

    private static <T> Collector<T, ?, Set<T>> toSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }

    private static <T> Collector<T, ?, SortedSet<T>> toSortedSet() {
        return Collectors.toCollection(TreeSet::new);
    }

    private static <T> Collector<T, ?, NavigableSet<T>> toNavigableSet() {
        return Collectors.toCollection(TreeSet::new);
    }

    private static <T> Collector<T, ?, Queue<T>> toQueue() {
        return Collectors.toCollection(LinkedList::new);
    }

    private static <T> Collector<T, ?, Deque<T>> toDeque() {
        return Collectors.toCollection(LinkedList::new);
    }

    private ManyGetterSetterRegistry() {
    }

    static <Structure, Value> ManyGetterSetter<Structure, Value> getFor(Class<?> clazz) {
        @SuppressWarnings("unchecked") var result = (ManyGetterSetter<Structure, Value>) MANY_GETTER_SETTERS_BY_CLASS.get(clazz);

        if (result == null) {
            throw new RuntimeException("No shorthand syntax allowed for " + clazz.getName());
        }

        return result;
    }
}

public interface ManyGetterSetter<Structure, Value> extends Streamer<Structure, Value>, Setter<Structure, Value>, Modifiable<Structure, Value> {

    static <Structure, Value> ManyGetterSetter<Structure, Value> from(Streamer<Structure, Value> streamer, Collector<Value, ?, Structure> collector) {
        var modifiable = Modifiable.from(streamer, collector);
        return new ManyGetterSetterImpl<>(streamer, modifiable);
    }

    static <Structure, Value> ManyGetterSetter<Structure, Value> from(SerializableFunction<Structure, ? extends Collection<Value>> methodReference) {
        var namedMethod = NamedMethod.from(methodReference);
        var methodName = namedMethod.getMethodName();
        var clazz = namedMethod.getTargetClass();

        if (!clazz.isRecord()) {
            throw new RuntimeException("Class " + clazz.getName() + " is not a record");
        }

        var component = Arrays.stream(clazz.getRecordComponents()).filter(recordComponent -> recordComponent.getName().equals(methodName)).findFirst().orElseThrow(() -> new RuntimeException("Method " + methodName + " is not a record component of " + clazz.getName()));

        var componentType = component.getType();

        if (!Collection.class.isAssignableFrom(componentType)) {
            throw new RuntimeException("Method " + methodName + " must return a collection");
        }

        return GetterSetter.from(methodReference).andThen(ManyGetterSetterRegistry.getFor(componentType));
    }

    default <T> ManyGetterSetter<Structure, T> andThen(ManyGetterSetter<Value, T> that) {
        var streamer = this.asStreamer().andThen(that);
        var modifiable = this.andThenModify(that);
        return new ManyGetterSetterImpl<>(streamer, modifiable);
    }
}
