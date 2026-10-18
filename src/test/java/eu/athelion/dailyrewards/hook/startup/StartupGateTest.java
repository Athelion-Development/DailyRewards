package eu.athelion.dailyrewards.hook.startup;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class StartupGateTest {
    @Test void waitsWithoutCallingItemConsumers() {
        AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> false, calls::incrementAndGet, failNow());
        gate.tryStart();
        assertEquals(0, calls.get()); assertEquals(StartupGate.State.WAITING, gate.state());
    }
    @Test void alreadyReadyInitializesImmediately() {
        AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> true, calls::incrementAndGet, failNow());
        gate.tryStart(); assertEquals(1, calls.get()); assertEquals(StartupGate.State.READY, gate.state());
    }
    @Test void delayedReadyEventInitializesOnce() {
        AtomicBoolean ready = new AtomicBoolean(); AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(ready::get, calls::incrementAndGet, failNow());
        gate.tryStart(); ready.set(true); gate.tryStart(); gate.tryStart();
        assertEquals(1, calls.get()); assertEquals(StartupGate.State.READY, gate.state());
    }
    @Test void falseOrDuplicateEventsDoNotBypassReadiness() {
        AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> false, calls::incrementAndGet, failNow());
        for (int i = 0; i < 20; i++) gate.tryStart();
        assertEquals(0, calls.get());
    }
    @Test void timeoutFailsClosedExactlyOnce() {
        AtomicInteger calls = new AtomicInteger(); AtomicInteger errors = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> false, calls::incrementAndGet, e -> errors.incrementAndGet());
        gate.timeout(); gate.timeout(); gate.tryStart();
        assertEquals(0, calls.get()); assertEquals(1, errors.get()); assertEquals(StartupGate.State.FAILED, gate.state());
    }
    @Test void lateReadinessCannotResurrectFailedStartup() {
        AtomicBoolean ready = new AtomicBoolean(); AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(ready::get, calls::incrementAndGet, e -> {});
        gate.timeout(); ready.set(true); gate.tryStart();
        assertEquals(0, calls.get()); assertEquals(StartupGate.State.FAILED, gate.state());
    }
    @Test void timeoutRecoversMissedEventWhenRegistryIsReady() {
        AtomicBoolean ready = new AtomicBoolean(); AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(ready::get, calls::incrementAndGet, failNow());
        gate.tryStart(); ready.set(true); gate.timeout();
        assertEquals(1, calls.get()); assertEquals(StartupGate.State.READY, gate.state());
    }
    @Test void timeoutDoesNotReinitializeReadyPlugin() {
        AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> true, calls::incrementAndGet, failNow());
        gate.tryStart(); gate.timeout(); assertEquals(1, calls.get());
    }
    @Test void shutdownPreventsQueuedEventAndTimeout() {
        StartupGate gate = new StartupGate(() -> true, () -> fail("initialized after shutdown"), failNow());
        gate.close(); gate.tryStart(); gate.timeout();
        assertEquals(StartupGate.State.CLOSED, gate.state());
    }
    @Test void initializationFailureDoesNotRetryOrIgnoreError() {
        RuntimeException error = new IllegalStateException("fixture");
        AtomicReference<Throwable> caught = new AtomicReference<>();
        StartupGate gate = new StartupGate(() -> true, () -> { throw error; }, caught::set);
        gate.tryStart(); gate.tryStart();
        assertSame(error, caught.get()); assertEquals(StartupGate.State.FAILED, gate.state());
    }
    @Test void linkageFailureFailsClosed() {
        AtomicReference<Throwable> caught = new AtomicReference<>();
        StartupGate gate = new StartupGate(() -> true, () -> { throw new NoSuchMethodError("fixture"); }, caught::set);
        gate.tryStart(); assertInstanceOf(NoSuchMethodError.class, caught.get());
        assertEquals(StartupGate.State.FAILED, gate.state());
    }
    @Test void readinessProbeFailureFailsClosed() {
        AtomicReference<Throwable> caught = new AtomicReference<>();
        StartupGate gate = new StartupGate(() -> { throw new IllegalStateException("fixture"); }, () -> fail(), caught::set);
        gate.tryStart(); assertNotNull(caught.get()); assertEquals(StartupGate.State.FAILED, gate.state());
    }
    @Test void reentrantCallbackCannotInitializeTwice() {
        AtomicReference<StartupGate> ref = new AtomicReference<>(); AtomicInteger calls = new AtomicInteger();
        StartupGate gate = new StartupGate(() -> true, () -> { calls.incrementAndGet(); ref.get().tryStart(); }, failNow());
        ref.set(gate); gate.tryStart(); assertEquals(1, calls.get());
    }
    @Test void shutdownDuringInitializationDoesNotBecomeReady() {
        AtomicReference<StartupGate> ref = new AtomicReference<>();
        StartupGate gate = new StartupGate(() -> true, () -> ref.get().close(), failNow());
        ref.set(gate); gate.tryStart(); assertEquals(StartupGate.State.CLOSED, gate.state());
    }
    private static java.util.function.Consumer<Throwable> failNow() {
        return error -> { throw new AssertionError(error); };
    }
}
