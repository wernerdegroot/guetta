package nl.wernerdegroot.guetta.core.optics.supporting;

@FunctionalInterface
public interface StateReader<Value, State> {

    Value run(State state);
}
