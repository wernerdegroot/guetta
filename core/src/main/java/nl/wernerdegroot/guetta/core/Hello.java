package nl.wernerdegroot.guetta.core;

import nl.wernerdegroot.guetta.core.optics.instances.ListManyGetterSetter;

import java.util.List;
import java.util.function.Function;

public class Hello {

    public static void main(String[] args) {
        List<Integer> xs = List.of(1, 2, 3);

        Function<Integer, List<Integer>> doublePositive =
                x -> List.of(x, -x);

        var traversal = new ListManyGetterSetter<Integer>();
        var result = traversal.modifyWithList(xs, doublePositive);

        System.out.println(result);
    }
}
