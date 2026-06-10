package nl.wernerdegroot.guetta.core.optics.internal.builders;

import java.util.ArrayList;

public class ArrayListBuilder<Value> implements Builder<ArrayList<Value>, ArrayList<Value>, Value> {

    @Override
    public ArrayList<Value> getAccumulator() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<Value> addValue(ArrayList<Value> structure, Value value) {
        var copy = new ArrayList<>(structure);
        copy.add(value);
        return copy;
    }

    @Override
    public ArrayList<Value> build(ArrayList<Value> structure) {
        return structure;
    }
}
