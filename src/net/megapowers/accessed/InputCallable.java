package net.megapowers.accessed;

import java.util.concurrent.Callable;

public abstract class InputCallable<T, E> implements Callable<E> {

    protected T input;
    public InputCallable(T input) {
        this.input = input;
    }
}
