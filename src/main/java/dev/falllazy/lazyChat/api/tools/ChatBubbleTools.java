package dev.falllazy.lazyChat.api.tools;

import dev.falllazy.lazyChat.api.events.ChatBubbleCreateEvent;
import dev.falllazy.lazyChat.api.events.ChatBubbleDestroyEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ChatBubbleTools {

    private final Map<UUID, TextDisplay> bubbleMap = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> byOwner = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> ownerOf = new ConcurrentHashMap<>();

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Plugin plugin;
    private BukkitTask cleanupTask;

    public ChatBubbleTools(Plugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }


    public void createBubble(String text, Location location, LivingEntity owner) {
        if (isValidInput(text, location, owner)) return;
        createBubbleI(mm.deserialize(text), location, owner);
    }

    public void createBubble(Component text, Location location, LivingEntity owner) {
        if (isValidInput(text, location, owner)) return;
        createBubbleI(text, location, owner);
    }

    private boolean isValidInput(Object text, Location location, LivingEntity owner) {
        if (!FeatureChecker.Bubble.isSupported()) return true;
        if (owner == null || owner.isDead() || !owner.isValid()) return true;
        if (text == null) return true;
        if (text instanceof String s && s.isBlank()) return true;
        return location == null || location.getWorld() == null;
    }

    private void createBubbleI(Component message, Location location, LivingEntity owner) {
        ChatBubbleCreateEvent event = new ChatBubbleCreateEvent(owner, false, message, location, null);

        TextDisplay spawned = location.getWorld().spawn(location, TextDisplay.class, e -> {
            e.text(event.getMessageContent());
            e.setLineWidth(50);
            e.setBackgroundColor(Color.BLACK);
            e.setBillboard(TextDisplay.Billboard.CENTER);
            e.setPersistent(false);
            e.setViewRange(0.5f);
        });

        event.setBubble(spawned);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            TextDisplay toRemove = event.getBubble() != null ? event.getBubble() : spawned;
            if (toRemove.isValid()) toRemove.remove();
            return;
        }

        TextDisplay finalBubble = event.getBubble() != null ? event.getBubble() : spawned;
        register(finalBubble, owner.getUniqueId());
    }

    private void register(TextDisplay bubble, UUID ownerId) {
        UUID bubbleId = bubble.getUniqueId();
        bubbleMap.put(bubbleId, bubble);
        byOwner.computeIfAbsent(ownerId, k -> ConcurrentHashMap.newKeySet()).add(bubbleId);
        ownerOf.put(bubbleId, ownerId);
    }


    public void destroyBubble(@Nullable TextDisplay bubble) {
        if (bubble == null) return;

        ChatBubbleDestroyEvent event = new ChatBubbleDestroyEvent(
                false, bubble.text(), bubble.getLocation(), bubble
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        destroyInternal(bubble.getUniqueId());
    }

    public void destroyBubble(@Nullable UUID bubbleId) {
        if (bubbleId == null) return;
        TextDisplay bubble = bubbleMap.get(bubbleId);
        if (bubble == null) return;
        destroyBubble(bubble);
    }

    public void clearOwner(@Nullable UUID ownerId) {
        if (ownerId == null) return;
        Set<UUID> ids = byOwner.remove(ownerId);
        if (ids == null || ids.isEmpty()) return;
        for (UUID id : new ArrayList<>(ids)) {
            destroyInternal(id);
        }
    }

    public void clearBubbles() {
        for (UUID id : new ArrayList<>(bubbleMap.keySet())) {
            destroyInternal(id);
        }
        bubbleMap.clear();
        byOwner.clear();
        ownerOf.clear();
    }

    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        clearBubbles();
    }

    private void destroyInternal(UUID bubbleId) {
        TextDisplay bubble = bubbleMap.remove(bubbleId);

        UUID ownerId = ownerOf.remove(bubbleId);
        if (ownerId != null) {
            Set<UUID> set = byOwner.get(ownerId);
            if (set != null) {
                set.remove(bubbleId);
                if (set.isEmpty()) byOwner.remove(ownerId, set);
            }
        }

        if (bubble != null && !bubble.isDead() && bubble.isValid()) {
            bubble.remove();
        }
    }

    @NotNull
    public Collection<TextDisplay> getBubbles(@Nullable UUID ownerId) {
        if (ownerId == null) return Collections.emptyList();
        Set<UUID> ids = byOwner.get(ownerId);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<TextDisplay> result = new ArrayList<>(ids.size());
        for (UUID id : ids) {
            TextDisplay bubble = bubbleMap.get(id);
            if (bubble != null && !bubble.isDead() && bubble.isValid()) {
                result.add(bubble);
            }
        }
        return result;
    }

    @Nullable
    public TextDisplay getBubble(@Nullable UUID ownerId) {
        Collection<TextDisplay> all = getBubbles(ownerId);
        return all.isEmpty() ? null : all.iterator().next();
    }

    public boolean hasBubbles(@Nullable UUID ownerId) {
        if (ownerId == null) return false;
        Set<UUID> ids = byOwner.get(ownerId);
        return ids != null && !ids.isEmpty();
    }

    public boolean isTracked(@Nullable TextDisplay bubble) {
        return bubble != null && bubbleMap.containsKey(bubble.getUniqueId());
    }

    public int size() {
        return bubbleMap.size();
    }

    public int size(@Nullable UUID ownerId) {
        if (ownerId == null) return 0;
        Set<UUID> ids = byOwner.get(ownerId);
        return ids == null ? 0 : ids.size();
    }


    private void startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID bubbleId : new ArrayList<>(bubbleMap.keySet())) {
                TextDisplay bubble = bubbleMap.get(bubbleId);
                if (bubble == null || bubble.isDead() || !bubble.isValid()) {
                    destroyInternal(bubbleId);
                }
            }
        }, 20L, 20L);
    }
}