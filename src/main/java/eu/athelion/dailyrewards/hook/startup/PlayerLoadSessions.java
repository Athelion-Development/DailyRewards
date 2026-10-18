package eu.athelion.dailyrewards.hook.startup;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Tracks player object identity, not Player.equals (which may compare only UUIDs). */
public final class PlayerLoadSessions<T> {
    private final ConcurrentMap<UUID, T> sessions = new ConcurrentHashMap<>();
    public boolean begin(UUID id, T session) { return sessions.putIfAbsent(id, session) == null; }
    public boolean owns(UUID id, T session) { return sessions.get(id) == session; }
    public void end(UUID id, T session) {
        sessions.computeIfPresent(id, (key, current) -> current == session ? null : current);
    }
}
