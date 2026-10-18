package eu.athelion.dailyrewards.hook.startup;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** A one-shot dependency lifecycle gate; callers execute it on the server scheduler. */
public final class StartupGate {
    public enum State { WAITING, STARTING, READY, FAILED, CLOSED }
    private final BooleanSupplier ready;
    private final Runnable initialize;
    private final Consumer<Throwable> failure;
    private State state = State.WAITING;

    public StartupGate(BooleanSupplier ready, Runnable initialize, Consumer<Throwable> failure) {
        this.ready = ready;
        this.initialize = initialize;
        this.failure = failure;
    }

    public void tryStart() {
        if (state != State.WAITING) return;
        try {
            if (!ready.getAsBoolean()) return;
            state = State.STARTING;
            initialize.run();
            if (state == State.STARTING) state = State.READY;
        } catch (RuntimeException | LinkageError error) {
            state = State.FAILED;
            failure.accept(error);
        }
    }

    public void timeout() {
        tryStart();
        if (state == State.WAITING) {
            state = State.FAILED;
            failure.accept(new IllegalStateException("ItemsAdder registry startup timed out after 2400 server ticks"));
        }
    }

    public void close() { state = State.CLOSED; }
    public State state() { return state; }
}
