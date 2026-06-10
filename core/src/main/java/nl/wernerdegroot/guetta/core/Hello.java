package nl.wernerdegroot.guetta.core;

import nl.wernerdegroot.guetta.core.optics.EachGetterSetterWithEffect;
import nl.wernerdegroot.guetta.core.optics.internal.CollectionEachGetterSetterWithEffect;

import java.util.List;
import java.util.function.Function;

public class Hello {

    public static void main(String[] args) {
        List<Integer> xs = List.of(1, 2, 3);

        Function<Integer, List<Integer>> doublePositive =
                x -> List.of(x, -x);

        var traversal = EachGetterSetterWithEffect.<List<Integer>, Integer>from(List.class);
        var result = traversal.modifyWithList(xs, doublePositive);

        System.out.println(result);
    }
}
