package eu.athelion.dailyrewards.hook.startup;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class PlayerLoadSessionsTest {
    @Test void allowsOneLoadPerSession() {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID(); Object player = new Object();
        assertTrue(s.begin(id, player)); assertFalse(s.begin(id, player)); assertTrue(s.owns(id, player));
    }
    @Test void anotherPlayerObjectCannotTakeActiveSession() {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID(); Object first = new Object(), second = new Object();
        assertTrue(s.begin(id, first)); assertFalse(s.begin(id, second)); assertTrue(s.owns(id, first)); assertFalse(s.owns(id, second));
    }
    @Test void reconnectCanStartAfterPreviousSessionEnds() {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID(); Object first = new Object(), second = new Object();
        s.begin(id, first); s.end(id, first); assertTrue(s.begin(id, second)); assertFalse(s.owns(id, first));
    }
    @Test void staleCallbackCannotRemoveNewSessionEvenIfPlayersCompareEqual() {
        PlayerLoadSessions<Token> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID(); Token first = new Token(), second = new Token();
        assertEquals(first, second);
        s.begin(id, first); s.end(id, first); s.begin(id, second); s.end(id, first);
        assertTrue(s.owns(id, second)); assertFalse(s.owns(id, first)); assertFalse(s.begin(id, first));
    }
    @Test void differentPlayersAreIndependent() {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID a = UUID.randomUUID(), b = UUID.randomUUID(); Object player = new Object();
        assertTrue(s.begin(a, player)); assertTrue(s.begin(b, player)); s.end(a, player); assertTrue(s.owns(b, player));
    }
    @Test void endingUnknownSessionIsHarmless() {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID();
        s.end(id, new Object()); assertTrue(s.begin(id, new Object()));
    }
    @Test void concurrentNotificationsHaveExactlyOneWinner() throws Exception {
        PlayerLoadSessions<Object> s = new PlayerLoadSessions<>(); UUID id = UUID.randomUUID(); AtomicInteger wins = new AtomicInteger();
        ExecutorService workers = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 100; i++) workers.submit(() -> { if (s.begin(id, new Object())) wins.incrementAndGet(); });
            workers.shutdown(); assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS)); assertEquals(1, wins.get());
        } finally { workers.shutdownNow(); }
    }
    private static final class Token {
        @Override public boolean equals(Object other) { return other instanceof Token; }
        @Override public int hashCode() { return 1; }
    }
}
