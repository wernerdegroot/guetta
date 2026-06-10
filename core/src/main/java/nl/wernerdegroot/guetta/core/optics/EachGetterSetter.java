package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.internal.EachGetterSetterImpl;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class ManyGetterSetterRegistry {

    private static final Map<Class<?>, EachGetterSetter<?, ?>> MANY_GETTER_SETTERS_BY_CLASS;

    static {

        // The following is inspired by MapStruct: https://mapstruct.org/documentation/stable/reference/html/#implementation-types-for-collection-mappings

        MANY_GETTER_SETTERS_BY_CLASS = new HashMap<>();

        // Interfaces
        MANY_GETTER_SETTERS_BY_CLASS.put(Collection.class, EachGetterSetter.from(Collection::stream, toCollection()));
        MANY_GETTER_SETTERS_BY_CLASS.put(List.class, EachGetterSetter.from(List::stream, toList()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Set.class, EachGetterSetter.from(Set::stream, toSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(SortedSet.class, EachGetterSetter.from(SortedSet::stream, toSortedSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(NavigableSet.class, EachGetterSetter.from(NavigableSet::stream, toNavigableSet()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Queue.class, EachGetterSetter.from(Queue::stream, toQueue()));
        MANY_GETTER_SETTERS_BY_CLASS.put(Deque.class, EachGetterSetter.from(Deque::stream, toDeque()));

        // List implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(ArrayList.class, EachGetterSetter.from(ArrayList::stream, Collectors.toCollection(ArrayList::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedList.class, EachGetterSetter.from(LinkedList::stream, Collectors.toCollection(LinkedList::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(Vector.class, EachGetterSetter.from(Vector::stream, Collectors.toCollection(Vector::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(Stack.class, EachGetterSetter.from(Stack::stream, Collectors.toCollection(Stack::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(CopyOnWriteArrayList.class, EachGetterSetter.from(CopyOnWriteArrayList::stream, Collectors.toCollection(CopyOnWriteArrayList::new)));

        // Set implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(HashSet.class, EachGetterSetter.from(HashSet::stream, Collectors.toCollection(HashSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedHashSet.class, EachGetterSetter.from(LinkedHashSet::stream, Collectors.toCollection(LinkedHashSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(TreeSet.class, EachGetterSetter.from(TreeSet::stream, Collectors.toCollection(TreeSet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(CopyOnWriteArraySet.class, EachGetterSetter.from(CopyOnWriteArraySet::stream, Collectors.toCollection(CopyOnWriteArraySet::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentSkipListSet.class, EachGetterSetter.from(ConcurrentSkipListSet::stream, Collectors.toCollection(ConcurrentSkipListSet::new)));

        // Queue / Deque implementations
        MANY_GETTER_SETTERS_BY_CLASS.put(PriorityQueue.class, EachGetterSetter.from(PriorityQueue::stream, Collectors.toCollection(PriorityQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ArrayDeque.class, EachGetterSetter.from(ArrayDeque::stream, Collectors.toCollection(ArrayDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentLinkedQueue.class, EachGetterSetter.from(ConcurrentLinkedQueue::stream, Collectors.toCollection(ConcurrentLinkedQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(ConcurrentLinkedDeque.class, EachGetterSetter.from(ConcurrentLinkedDeque::stream, Collectors.toCollection(ConcurrentLinkedDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedBlockingQueue.class, EachGetterSetter.from(LinkedBlockingQueue::stream, Collectors.toCollection(LinkedBlockingQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedBlockingDeque.class, EachGetterSetter.from(LinkedBlockingDeque::stream, Collectors.toCollection(LinkedBlockingDeque::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(PriorityBlockingQueue.class, EachGetterSetter.from(PriorityBlockingQueue::stream, Collectors.toCollection(PriorityBlockingQueue::new)));
        MANY_GETTER_SETTERS_BY_CLASS.put(LinkedTransferQueue.class, EachGetterSetter.from(LinkedTransferQueue::stream, Collectors.toCollection(LinkedTransferQueue::new)));

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

    static <Structure, Value> EachGetterSetter<Structure, Value> getFor(Class<?> clazz) {
        @SuppressWarnings("unchecked") var result = (EachGetterSetter<Structure, Value>) MANY_GETTER_SETTERS_BY_CLASS.get(clazz);

        if (result == null) {
            throw new RuntimeException("No shorthand syntax allowed for " + clazz.getName());
        }

        return result;
    }
}

public interface EachGetterSetter<Structure, Value> extends Streamer<Structure, Value>, Setter<Structure, Value> {

    static <Structure, Value> EachGetterSetter<Structure, Value> from(Streamer<Structure, Value> streamer, Collector<Value, ?, Structure> collector) {
        var setter = Setter.from(streamer, collector);
        return new EachGetterSetterImpl<>(streamer, setter);
    }

    default <T> EachGetterSetter<Structure, T> andThen(EachGetterSetter<Value, T> that) {
        var streamer = this.asStreamer().andThen(that);
        var setter = this.asSetter().andThen(that);
        return new EachGetterSetterImpl<>(streamer, setter);
    }

    @Override
    default EachGetterSetter<Structure, Value> filter(Predicate<Value> predicate) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    default EachGetterSetter<Structure, Value> take(int n) {
        return new EachGetterSetterImpl<>(
                structure -> asStreamer().stream(structure).limit(n),
                (structure, modifier) -> {
                    var counter = new Object() {
                        int count = 0;
                    };
                    return asSetter().modify(structure, value -> {
                        if (counter.count < n) {
                            counter.count++;
                            return modifier.apply(value);
                        } else {
                            return value;
                        }
                    });
                }
        );
    }

    @Override
    default EachGetterSetter<Structure, Value> takeWhile(java.util.function.Predicate<Value> predicate) {
        return new EachGetterSetterImpl<>(
                structure -> asStreamer().stream(structure).takeWhile(predicate),
                (structure, modifier) -> {
                    var state = new Object() {
                        boolean taking = true;
                    };
                    return asSetter().modify(structure, value -> {
                        if (state.taking && predicate.test(value)) {
                            return modifier.apply(value);
                        } else {
                            state.taking = false;
                            return value;
                        }
                    });
                }
        );
    }
}
