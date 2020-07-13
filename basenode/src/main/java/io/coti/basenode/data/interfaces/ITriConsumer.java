package io.coti.basenode.data.interfaces;

@FunctionalInterface
public interface ITriConsumer<R, S, T> {
    void accept(R r, S s, T t);
}
