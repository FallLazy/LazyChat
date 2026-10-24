package dev.falllazy.lazyChat.chatSystem;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownThings {
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public boolean checkAndUpdate(UUID playerId, double cooldownSeconds) {
        long now = System.currentTimeMillis();
        long cooldownMillis = (long) (cooldownSeconds * 1000);
        Long last = cooldowns.get(playerId);
        if (last != null && (now - last) < cooldownMillis) {
            return false;
        }
        cooldowns.put(playerId, now);
        return true;
    }

    public double getRemaining(UUID playerId, double cooldownSeconds) {
        long now = System.currentTimeMillis();
        long cooldownMillis = (long) (cooldownSeconds * 1000);
        Long last = cooldowns.get(playerId);
        if (last == null) return 0;
        long elapsed = now - last;
        if (elapsed >= cooldownMillis) return 0;
        return (cooldownMillis - elapsed) / 1000.0;
    }
}