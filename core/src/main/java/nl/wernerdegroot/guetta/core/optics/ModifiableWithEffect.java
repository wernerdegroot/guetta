package nl.wernerdegroot.guetta.core.optics;

import nl.wernerdegroot.guetta.core.optics.supporting.Continuation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface ModifiableWithEffect<Structure, Value> {

    <Result> Continuation<Result, Structure> modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect);

    default <Result> Result modifyWithEffect(Structure structure, Function<Value, Continuation<Result, Value>> modifierWithEffect, Function<Structure, Result> runner) {
        return modifyWithEffect(structure, modifierWithEffect).run(runner);
    }

    default Optional<Structure> modifyWithOptional(Structure structure, Function<Value, Optional<Value>> modifierWithOptional) {
        return modifyWithEffect(structure, Continuation.optional(modifierWithOptional), Optional::of);
    }

    default CompletableFuture<Structure> modifyWithCompletableFuture(Structure structure, Function<Value, CompletableFuture<Value>> modifierWithOptional) {
        return modifyWithEffect(structure, Continuation.completableFuture(modifierWithOptional), CompletableFuture::completedFuture);
    }

    default List<Structure> modifyWithList(Structure structure, Function<Value, List<Value>> modifierWithList) {
        return modifyWithEffect(structure, Continuation.list(modifierWithList), List::of);
    }
}
